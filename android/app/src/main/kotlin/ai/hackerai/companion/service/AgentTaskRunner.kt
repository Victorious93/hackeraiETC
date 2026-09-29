package ai.hackerai.companion.service

import ai.droidcommand.hackerai.CreateAgentInput
import ai.droidcommand.hackerai.DoomLoopCheckResult
import ai.droidcommand.hackerai.DoomLoopDetector
import ai.droidcommand.hackerai.MAX_SUBAGENT_SKILLS
import ai.droidcommand.hackerai.SUBAGENT_MAX_STEPS
import ai.droidcommand.hackerai.ResolveSkillsResult
import ai.droidcommand.hackerai.SecurityValidationCandidate
import ai.droidcommand.hackerai.SkillCatalog
import ai.droidcommand.hackerai.SkillRanker
import ai.droidcommand.hackerai.SubagentSkill
import ai.droidcommand.hackerai.classifyProviderError
import ai.droidcommand.hackerai.getSubagentProviderRetryDecision
import ai.droidcommand.hackerai.shouldStartResultRecovery
import ai.hackerai.companion.llm.LocalLlmProvider
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentTaskRunner @Inject constructor(
    private val llmProvider: LocalLlmProvider,
) {
    internal val executor: ExecutorService = Executors.newCachedThreadPool()
    private val activeTasks = ConcurrentHashMap<String, Pair<Future<*>, AtomicBoolean>>()
    private val json = Json { ignoreUnknownKeys = true }

    fun runTask(inputJson: String): String {
        val input = runCatching { json.decodeFromString<CreateAgentInput>(inputJson) }
            .getOrElse { e -> return errorJson("Invalid CreateAgentInput: ${e.message}") }

        val skillResult = if (!input.skills.isNullOrEmpty()) {
            SkillCatalog.resolveSkills(input.skills)
        } else {
            ResolveSkillsResult.Success(SkillCatalog.listSkills().take(MAX_SUBAGENT_SKILLS))
        }
        val resolvedSkills = when (skillResult) {
            is ResolveSkillsResult.Success -> skillResult.skills
            is ResolveSkillsResult.Failure -> return errorJson(skillResult.error)
        }
        val rankedSkills = if (resolvedSkills.size > MAX_SUBAGENT_SKILLS) {
            SkillRanker.rankSkillsForTask(input.task, resolvedSkills).take(MAX_SUBAGENT_SKILLS)
        } else {
            resolvedSkills
        }

        val taskId = UUID.randomUUID().toString()
        val cancelled = AtomicBoolean(false)
        val future = executor.submit { runTaskInternal(taskId, input, rankedSkills, cancelled) }
        activeTasks[taskId] = Pair(future, cancelled)

        return """{"ok":true,"taskId":"$taskId","status":"queued"}"""
    }

    /**
     * Runs a task to completion. Halts when:
     *  - the step budget is exhausted (shouldStartResultRecovery)
     *  - DoomLoopDetector fires HALT (≥5 identical responses)
     *  - the cancelled flag is set
     *  - a non-retryable provider error occurs
     */
    internal fun runTaskInternal(
        taskId: String,
        input: CreateAgentInput,
        skills: List<SubagentSkill>,
        cancelled: AtomicBoolean,
    ) {
        val doomLoop = DoomLoopDetector()
        val systemPrompt = buildSystemPrompt(input, skills)
        var step = 0
        var retriesUsed = 0

        while (step < SUBAGENT_MAX_STEPS && !cancelled.get()) {
            if (shouldStartResultRecovery(step, SUBAGENT_MAX_STEPS)) break

            val response = try {
                llmProvider.chat(systemPrompt = systemPrompt, userMessage = input.task, step = step)
            } catch (e: Exception) {
                val category = classifyProviderError(
                    statusCode = null,
                    exceptionName = e.javaClass.simpleName,
                )
                val decision = getSubagentProviderRetryDecision(category, retriesUsed)
                if (decision.shouldRetry) {
                    retriesUsed++
                    Thread.sleep(decision.delayMs)
                    continue
                }
                break
            }

            val resultElement = buildJsonObject { put("response", response) }
            when (val loopResult = doomLoop.check(resultElement)) {
                is DoomLoopCheckResult.Halt -> break
                is DoomLoopCheckResult.Warning -> { /* warn at ${loopResult.count} identical steps */ }
                is DoomLoopCheckResult.Ok -> { /* continue */ }
            }
            step++
        }
        activeTasks.remove(taskId)
    }

    fun getSkillCatalog(): String = runCatching {
        val skills = SkillCatalog.listSkills()
        val encoded = Json.encodeToString(ListSerializer(SubagentSkill.serializer()), skills)
        """{"ok":true,"skills":$encoded}"""
    }.getOrElse { e -> errorJson(e.message ?: "Failed to load skill catalog") }

    fun validateFinding(candidateJson: String): String {
        val candidate = runCatching { json.decodeFromString<SecurityValidationCandidate>(candidateJson) }
            .getOrElse { e -> return errorJson("Invalid SecurityValidationCandidate: ${e.message}") }
        val verdict = when {
            candidate.reproduction_hint != null && candidate.reproduction_hint.length >= 20 -> "CONFIRMED"
            else -> "NEEDS_MORE_EVIDENCE"
        }
        return """{"ok":true,"verdict":"$verdict"}"""
    }

    fun cancelTask(taskId: String) {
        activeTasks[taskId]?.let { (future, cancelled) ->
            cancelled.set(true)
            future.cancel(true)
        }
    }

    fun healthCheck(): String = if (llmProvider.hasApiKey()) "ok" else "no_llm_provider"

    private fun buildSystemPrompt(input: CreateAgentInput, skills: List<SubagentSkill>): String {
        val skillNames = skills.joinToString(", ") { it.name }
        val brief = input.brief?.let { "\n\n$it" } ?: ""
        return "You are a security expert. Available skills: $skillNames$brief"
    }

    private fun errorJson(message: String): String {
        val escaped = message.replace("\\", "\\\\").replace("\"", "\\\"")
        return """{"ok":false,"error":"$escaped"}"""
    }
}
