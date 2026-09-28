// IHackerAIService.aidl — AIDL contract between DroidCommand AI and the HackerAI companion APK.
// Keep this file in sync with core-companion/src/main/aidl/ai/droidcommand/companion/IHackerAIService.aidl
// in the droidcommand-AI repository. All parameters and return values are JSON strings to keep
// the interface stable as capabilities are added.
package ai.droidcommand.companion;

interface IHackerAIService {
    // Run an agent task. inputJson is a CreateAgentInput JSON object.
    // Returns a taskId string on success, or an error JSON {"error": "..."}.
    String runAgentTask(String inputJson);

    // Returns the full SubagentSkill[] catalog as a JSON array.
    String getSkillCatalog();

    // Validate a security finding candidate. candidateJson is a SecurityValidationCandidate JSON.
    // Returns a SecurityValidationResult JSON.
    String validateFinding(String candidateJson);

    // Cancel a running task. Fire-and-forget (oneway).
    oneway void cancelTask(String taskId);

    // Health check. Returns "ok", "busy", or "no_llm_provider".
    String healthCheck();
}
