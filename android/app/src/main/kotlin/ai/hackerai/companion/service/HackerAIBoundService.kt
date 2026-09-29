package ai.hackerai.companion.service

import ai.droidcommand.companion.IHackerAIService
import ai.hackerai.companion.DependencyGuard
import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HackerAIBoundService : Service() {
    @Inject lateinit var agentTaskRunner: AgentTaskRunner
    @Inject lateinit var dependencyGuard: DependencyGuard

    private val binder =
        object : IHackerAIService.Stub() {
            override fun runAgentTask(inputJson: String): String = this@HackerAIBoundService.runAgentTask(inputJson)
            override fun getSkillCatalog(): String = this@HackerAIBoundService.getSkillCatalog()
            override fun validateFinding(candidateJson: String): String = this@HackerAIBoundService.validateFinding(candidateJson)
            override fun cancelTask(taskId: String) = agentTaskRunner.cancelTask(taskId)
            override fun healthCheck(): String = this@HackerAIBoundService.healthCheck()
        }

    override fun onBind(intent: Intent): IBinder = binder

    fun runAgentTask(inputJson: String): String {
        if (!dependencyGuard.isDcaInstalled()) return errorJson("DroidCommand AI is not installed")
        return agentTaskRunner.runTask(inputJson)
    }

    fun getSkillCatalog(): String {
        if (!dependencyGuard.isDcaInstalled()) return errorJson("DroidCommand AI is not installed")
        return agentTaskRunner.getSkillCatalog()
    }

    fun validateFinding(candidateJson: String): String {
        if (!dependencyGuard.isDcaInstalled()) return errorJson("DroidCommand AI is not installed")
        return agentTaskRunner.validateFinding(candidateJson)
    }

    fun healthCheck(): String {
        if (!dependencyGuard.isDcaInstalled()) return "dca_not_installed"
        return agentTaskRunner.healthCheck()
    }

    private fun errorJson(message: String): String {
        val escaped = message.replace("\\", "\\\\").replace("\"", "\\\"")
        return """{"ok":false,"error":"$escaped"}"""
    }
}
