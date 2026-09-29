# HackerAI Companion APK — Architecture

## Component diagram

```
DroidCommand AI (:app)
  └── bindService(IHackerAIService, BIND_HACKERAI)  [signature permission]
        └── HackerAIBoundService                     [Service + IHackerAIService.Stub]
              ├── DependencyGuard                     [PackageManager check at every call]
              └── AgentTaskRunner                     [STUB → Phase 5: core-hackerai delegation]
                    ├── SkillCatalog                  [core-hackerai: loads strix catalog]
                    ├── DoomLoopDetector              [core-hackerai: warn@3, halt@5]
                    ├── RuntimeRecovery               [core-hackerai: provider error retry]
                    └── LocalLlmProvider              [configured API key / local model]
```

## Runtime security model

1. **Bind-time**: Android OS checks that the binding app holds
   `ai.droidcommand.permission.BIND_HACKERAI`. This is a `protectionLevel="signature"`
   permission declared in DCA's `AndroidManifest.xml`. Only apps signed with the same
   signing certificate as DCA receive this permission — all others are rejected at the
   OS level before any Java/Kotlin code runs.

2. **Call-time**: `HackerAIBoundService` delegates every AIDL call to `AgentTaskRunner`
   after a `DependencyGuard.isDcaInstalled()` check. If DCA was uninstalled after binding,
   the check returns `false` and the call returns an error JSON without executing.

3. **No inbound network**: The companion has no HTTP server. All communication is via
   AIDL on the local device — there is no loopback port to attack.

## Data flow for a runAgentTask call

```
DCA: agentTool.invoke(inputJson)
  → IHackerAIService.Stub.runAgentTask(inputJson)      [Binder thread]
  → DependencyGuard.isDcaInstalled() → true
  → AgentTaskRunner.runTask(inputJson)
    → parse CreateAgentInput (core-hackerai)
    → SkillCatalog.resolveSkills(input.skills)
    → SkillRanker.rankSkillsForTask() if > MAX_SUBAGENT_SKILLS candidates
    → LlmProvider.chat(systemPrompt, messages) [stream]
    → DoomLoopDetector.check(step) → warn/halt if loop detected
    → return {"task_id":"...","status":"queued"}        [immediately]
```

(Phase 5: full async task tracking with progress callbacks)

## Component status (updated 2026-09-29, Phase 5)

| Component                 | Status                             | Notes                                                                    |
| ------------------------- | ---------------------------------- | ------------------------------------------------------------------------ |
| IHackerAIService.aidl     | IMPLEMENTED                        | JSON string transport; stable interface                                  |
| HackerAIBoundService      | IMPLEMENTED                        | DependencyGuard + Hilt injection; refactored to expose delegate methods  |
| DependencyGuard           | IMPLEMENTED                        | PackageManager check; tested with mockk                                  |
| AgentTaskRunner           | IMPLEMENTED — NOT RUNTIME VERIFIED | SkillCatalog, DoomLoopDetector, RuntimeRecovery, StepBudgetGate wired in |
| LocalLlmProvider          | IMPLEMENTED — NOT RUNTIME VERIFIED | EncryptedSharedPreferences + HttpURLConnection; no Android SDK to build  |
| StatusScreen              | IMPLEMENTED                        | DCA connection state display                                             |
| SkillBrowserScreen        | IMPLEMENTED — NOT RUNTIME VERIFIED | TF-IDF search over SkillCatalog; LazyColumn with category badges         |
| SettingsScreen            | IMPLEMENTED — NOT RUNTIME VERIFIED | API key (masked), model, endpoint inputs wired to LocalLlmProvider       |
| HackerAINavHost           | IMPLEMENTED — NOT RUNTIME VERIFIED | Bottom nav: Status / Skills / Settings; real NavHost + NavController     |
| core-hackerai integration | IMPLEMENTED — NOT RUNTIME VERIFIED | Requires `./gradlew :core-hackerai:publishToMavenLocal` before AGP build |
