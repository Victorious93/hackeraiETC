# hackeraiETC — architecture (fork-specific)

Not synced from upstream — see `CODEX.md` for why this fork carries its
own docs alongside upstream's `CLAUDE.md`/`AGENTS.md`. This file describes
what's actually in this codebase and, specifically, what's relevant to
this fork's role in Victorious93's fleet (as the source for a port into
`Victorious93/droidcommand-AI`). It is **not** a general HackerAI
contributor guide — read upstream's own `AGENTS.md` for that.

## What this repo is

A maintained fork of `hackerai-tech/hackerai` — "HackerAI," a Next.js 15 /
TypeScript / Convex web app ("Your AI-Powered Penetration Testing
Assistant," per its own README), licensed Apache 2.0 "with Commercial
Restrictions." It is a hosted SaaS chat product with billing (Stripe),
multi-tenant auth (WorkOS), analytics (PostHog), and isolated cloud
execution (E2B) — **it is not itself an Android app and does not build an
APK.** Its role in the fleet is as a source codebase: specific parts of
its AI-agent/tool-orchestration domain are being ported (clean-room, not
copied) into droidcommand-AI's own Kotlin modules. Anything below outside
that domain (chat UI, billing, Convex schema, most of `lib/ai/tools/`) is
**not audited** — don't infer it's irrelevant just because it's absent
from this document; `docs/HACKERAI_SOURCE_AUDIT.md` in droidcommand-AI
says the same.

Verified 2026-09-29 against a fresh clone of `main` (single commit,
depth 1): Next.js `app/` router, Convex backend (`convex/`), ~696
TypeScript files under `lib/`, Playwright e2e (`e2e/`) and Jest unit
(`__tests__/`, `jest.config.js`) test setups, a `packages/` workspace
(`local`, `desktop` — separate sandbox/desktop builds), Docker sandbox
image (`docker/`), Trigger.dev durable-task config (`trigger.config.ts`),
and `.agents/skills/` (an internal skills directory distinct from the
vendored Strix pentest skill catalog under `third_party/` that
`docs/HACKERAI_SOURCE_AUDIT.md` references).

## The audited domain: `lib/ai/subagents/` and `lib/api/agent-*.ts`

This is the only part of the codebase droidcommand-AI's audit actually
read closely (per that audit's own scope note). Summary of what's there,
reproduced from `docs/HACKERAI_SOURCE_AUDIT.md` (droidcommand-AI repo) so
this fork's docs don't drift from that audit — read that file directly
for the full detail, this is an index:

| Component | File | What it does |
|---|---|---|
| Multi-agent delegation contracts | `lib/ai/subagents/contracts.ts` | `create_agent`/`delegate_task`/`send_message_to_agent`/`wait_for_agents`/`list_agents`/`cancel_agent`; per-agent budget, step limit, wall-clock bounds |
| Runtime re-authorization | `runtime-authorization.ts` | Re-checks a child agent's run validity at each tool call, not just at spawn |
| Step-budget reservation | `runtime-recovery.ts` | Reserves the last N steps to force a structured result instead of a hard failure when a budget runs out |
| Provider error-category retry | `runtime-recovery.ts` | Classifies LLM call failures (`rate_limited`, `provider_5xx`, `stream_terminated`, `timeout`, `content_blocked`) and retries only transient ones, exponential backoff + jitter |
| Compact agent handles | `agent-handle.ts` | Short model-facing IDs vs. durable internal IDs |
| Evidence-gated security schemas | `contracts.ts` (`security_task`/`security_validation`) | Structured pentest-finding results that require evidence before a CONFIRMED verdict |
| Strix pentest skill catalog | `lib/ai/subagents/skills/`, vendored under `third_party/` | ~62-entry domain skill library for pentest tasks |
| Agent HTTP route lifecycle | `agent-approval-route.ts`, `agent-cancel-route.ts`, `agent-resume-route.ts`, `agent-status-route.ts`, `agent-trigger-route.ts` | Web-transport-specific; the underlying state machine is the portable part |
| Billing/rate-limit/region guard | `billing.ts`, `rate-limit-finalization.ts`, `region-guard.ts` | Hosted-SaaS-specific, not applicable to a library/APK |

## What has actually been ported (status as of 2026-09-29)

**Corrects `CODEX.md`'s earlier 2026-09-27 status** — the port is further
along than "not started." droidcommand-AI's `docs/AUDIT_2026-09-05.md`
addendum dated **2026-09-28** ("Phase 3: core-hackerai and
core-pentest-swarm modules") records a new **`core-hackerai`** JVM module
in that repo, Kotlin ports of this codebase's subagent orchestration
logic:

- `SubagentContracts.kt` — mirrors `contracts.ts`'s Zod schemas as
  `@Serializable` Kotlin data classes, with the CONFIRMED-requires-evidence
  invariant enforced in `init {}` blocks.
- `SkillCatalog.kt` — loads a generated `strix-skill-catalog.generated.json`
  (62 entries, copied from this repo's `lib/ai/subagents/skills/`).
- `DoomLoopDetector.kt`, `RuntimeRecovery.kt`, `StepBudgetGate.kt`,
  `DelegationBudget.kt`, `EvidenceReference.kt`, `SkillRanker.kt` — each
  ports one of the mechanisms in the table above.
- `HackerAiTool.kt` — a `Tool` stub (`run_hackerai_agent_task`) that
  returns `ToolResult.Failure` until droidcommand-AI's Phase 4 (an AIDL
  companion-service binding, Android-SDK-gated) exists to actually call
  into a running HackerAI-derived agent.

35 tests pass for that module in droidcommand-AI
(`./gradlew :core-hackerai:test`). **What this means for this fork:** the
"clean-room reimplementation, never by copying this repo's source" plan
`CODEX.md` originally described has been executed for the mechanisms
above — nothing in `core-hackerai` is copied source from here, and this
repo's own code is unmodified (no PR here corresponds to that work; it
lives entirely in `droidcommand-AI`).

**What's still not done, honestly:** the multi-agent delegation contracts
themselves (`create_agent`/`delegate_task`/etc., item 1 in the table
above) have **not** been ported — droidcommand-AI's `ObjectiveEngine`
still runs exactly one agent loop per session. `HackerAiTool` is a stub
pending the AIDL companion binding. The HTTP route lifecycle and
billing/rate-limit/region-guard rows are correctly classified as not
applicable and are not being ported at all.

## Fleet relationship

```
hackeraiETC (this repo)          — source; unmodified; audited, not built from
        |  (clean-room port, selected mechanisms)
        v
droidcommand-AI: core-hackerai   — VERIFIED IMPLEMENTED (2026-09-28), JVM, 35 tests
        |  (depends on, not yet built)
        v
droidcommand-AI: core-companion  — PLANNED (Phase 4), Android-SDK-gated,
                                    AIDL binding that would let HackerAiTool
                                    actually call a running agent
```

Pentest-Swarm-AI (the third fleet repo, not added to this session — access
was declined) is integrated the same way via a separate `core-pentest-swarm`
JVM module (same 2026-09-28 addendum), bridging to its Go server over REST
rather than AIDL.
