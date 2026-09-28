package ai.hackerai.companion.service

import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentTaskRunner @Inject constructor() {
    private val TAG = "AgentTaskRunner"
    private val activeTasks = ConcurrentHashMap<String, AtomicBoolean>()

    fun runTask(inputJson: String): String {
        // Stub: core-hackerai not yet on mavenLocal in this build environment.
        // Replace with real SkillCatalog/DoomLoopDetector/LocalLlmProvider delegation
        // once core-hackerai:0.1.0-SNAPSHOT is published.
        Log.d(TAG, "runTask called with: $inputJson")
        val taskId = java.util.UUID.randomUUID().toString()
        activeTasks[taskId] = AtomicBoolean(false)
        return """{"task_id":"$taskId","status":"queued"}"""
    }

    fun getSkillCatalog(): String {
        // Stub: return empty catalog until core-hackerai is available.
        return """{"skills":[]}"""
    }

    fun validateFinding(candidateJson: String): String {
        // Stub: return inconclusive until core-hackerai validation is wired in.
        Log.d(TAG, "validateFinding called with: $candidateJson")
        return """{"verdict":"inconclusive","confidence":"low","evidence_refs":[]}"""
    }

    fun cancelTask(taskId: String) {
        activeTasks[taskId]?.set(true)
    }

    fun healthCheck(): String {
        return "ok"
    }
}
