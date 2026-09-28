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

## Component status

| Component                 | Status            | Notes                                                     |
| ------------------------- | ----------------- | --------------------------------------------------------- |
| IHackerAIService.aidl     | IMPLEMENTED       | JSON string transport; stable interface                   |
| HackerAIBoundService      | IMPLEMENTED       | DependencyGuard + Hilt injection                          |
| DependencyGuard           | IMPLEMENTED       | PackageManager check; tested with mockk                   |
| AgentTaskRunner           | STUB              | Returns placeholder JSON; Phase 5 wires core-hackerai     |
| StatusScreen              | IMPLEMENTED       | DCA connection state display                              |
| HackerAINavHost           | STUB              | Placeholder; Phase 5 adds SkillBrowser + Settings screens |
| core-hackerai integration | PLANNED (Phase 5) | Requires publishToMavenLocal                              |
