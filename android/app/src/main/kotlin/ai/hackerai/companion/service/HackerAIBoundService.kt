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
            override fun runAgentTask(inputJson: String): String {
                if (!dependencyGuard.isDcaInstalled()) {
                    return errorJson("DroidCommand AI is not installed")
                }
                return agentTaskRunner.runTask(inputJson)
            }

            override fun getSkillCatalog(): String {
                if (!dependencyGuard.isDcaInstalled()) {
                    return errorJson("DroidCommand AI is not installed")
                }
                return agentTaskRunner.getSkillCatalog()
            }

            override fun validateFinding(candidateJson: String): String {
                if (!dependencyGuard.isDcaInstalled()) {
                    return errorJson("DroidCommand AI is not installed")
                }
                return agentTaskRunner.validateFinding(candidateJson)
            }

            override fun cancelTask(taskId: String) {
                agentTaskRunner.cancelTask(taskId)
            }

            override fun healthCheck(): String = agentTaskRunner.healthCheck()
        }

    override fun onBind(intent: Intent): IBinder = binder

    private fun errorJson(message: String): String = """{"error":"$message"}"""
}
