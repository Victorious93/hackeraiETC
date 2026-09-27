# Claude vs. Codex: Task Assignment for HackerAI

Draft for review. Not wired into `AGENTS.md`/`codex.md` — those remain the single
shared instruction set for all coding agents per existing convention. This is a
separate, optional reference for a human (or a session) deciding which agent to
hand a given task to. Not committed or pushed pending approval.

## How confident is this, really

[Guessing]/[Likely], not [Certain]. External research for this doc came from
public blog/comparison sites (Medium, morphllm.com, DataCamp, codersera.com,
etc.), not primary benchmark publications. Those sources disagree with each
other on exact model names and scores as of September 2026 — e.g. SWE-bench
Verified numbers cited for "the current Claude" range from 85.2% to 97.0%
depending on the source and which model generation they mean, and OpenAI has
stopped publishing SWE-bench Verified for its newest models because the
benchmark is saturated (top models cluster within ~1 point of each other).
Treat every specific number below as unreliable. What's more consistent
across independent sources is the *qualitative* trait pattern, which is what
this assignment is actually based on:

- **Claude Code**: reasoning depth, higher autonomy with self-correction on
  long multi-step tasks, stronger relative performance on harder/more-realistic
  benchmarks (SWE-bench Pro over SWE-bench Verified), first-class long-context
  and cross-file reasoning.
- **Codex CLI**: speed and token/cost efficiency, strong on terminal-native
  execution benchmarks, sustained multi-hour autonomous runs via its `/goal`
  mode (one stress test cited: ~25 hours, ~13M tokens, ~30k LOC), up to 6
  concurrent subagents for parallelizable work, open-source and generally
  cheaper per task.
- Recurring bottom line across sources: **use both**, matched to task shape
  rather than picking one as strictly "better."

Sources: [Claude Code vs Codex vs OpenCode (Medium)](https://medium.com/@unicodeveloper/claude-code-vs-codex-vs-opencode-which-ai-coding-agent-is-actually-the-best-in-2026-baa9f6fd5374), [Codex vs Claude Code, Sept 2026 (morphllm)](https://www.morphllm.com/comparisons/codex-vs-claude-code), [Claude Benchmarks 2026 (morphllm)](https://www.morphllm.com/claude-benchmarks), [Codex vs. Claude Code (DataCamp)](https://www.datacamp.com/blog/codex-vs-claude-code), [Run long horizon tasks with Codex (OpenAI Developers)](https://developers.openai.com/blog/run-long-horizon-tasks-with-codex), [Agentic AI Comparison: Claude Code vs Codex CLI](https://aiagentstore.ai/compare-ai-agents/claude-code-vs-codex-cli)

## What this repo actually contains

(From a fresh read-only pass over the checked-out tree: 1,674 tracked files,
~380,600 lines of TS/TSX/JS.)

- **Next.js app** (`app/`, 473 files) — UI + HTTP route handlers.
- **Convex** (`convex/`, 118 files) — persisted data; `schema.ts` (1,573 lines)
  cascades into every query/mutation; `chats.ts`/`messages.ts` run 2,300–2,500
  lines each.
- **Trigger.dev** (`trigger/`, 14 files) — `agent-long.ts` (5,409 lines) is the
  single largest, most complex file in the repo: durable job orchestration,
  billing, cancellation/retry, Centrifugo streaming, subagent contracts.
- **Shared streaming boundary**: `lib/api/agent-stream-runner.ts` (2,697 lines),
  called from both `trigger/agent-long.ts` (Agent path) and
  `lib/api/chat-handler.ts` (3,163 lines, Ask path) — AGENTS.md's "trace both
  callers" warning is concrete here, backed by 5 dedicated cross-path test files.
- **Sandbox transports** — three genuinely separate ones: `packages/local/`
  (Node/TS local agent), `packages/desktop/` (Tauri app with real Rust:
  `src-tauri/src/*.rs`), and `e2b/` (cloud, region-aware templates). Bridged by
  Centrifugo (`lib/ai/tools/utils/centrifugo-sandbox.ts`, 2,302 lines + a
  3,268-line test).
- **AI subagents/prompt engineering**: `lib/ai/subagents/` (40 files: contracts,
  billing, sandbox-identity, model-routing, runtime-authorization); `lib/ai/tools/`
  (125 files, one tool per file + test); `third_party/strix-skills/` (65 files,
  vendored pentesting-skill taxonomy).
- **Security-specific**: sandbox execution safety, region-guarding, private-
  artifact hardening, `scripts/validate-s3-security.ts`.
- **Tests**: root `__tests__/` + `__mocks__/` (22 mocks) + co-located tests in
  nearly every module + `e2e/` (28 files, Playwright).
- **Scripts/CI**: 30 scripts, 5 GitHub Actions workflows.
- **`.agents/skills/`**: only 2 HackerAI-owned skills (deliberately small, per
  AGENTS.md's "don't vendor generic skills" rule).

## Assignment

### Lean Claude: deep, cross-cutting, or judgment-heavy work

Rationale: these tasks require holding multiple call sites/services in mind at
once, or making a defensible tradeoff call rather than following a spec — the
trait every source attributes to Claude Code's reasoning-depth advantage.

- Changes to `lib/api/agent-stream-runner.ts` (must verify both the Ask and
  Agent callers stay in sync).
- Changes to `trigger/agent-long.ts` (billing + cancellation/retry + streaming
  + subagent-contract interactions in one file).
- Changes to `lib/ai/subagents/contracts.ts` or `runtime-authorization.ts`
  (ripples through 17 dependent test files across billing, sandbox-identity,
  parent-delivery).
- `convex/schema.ts` changes (cascades into every query/mutation file).
- Any change touching more than one of the three sandbox transports
  (`packages/local`, `packages/desktop`, `e2b/`) plus Centrifugo — AGENTS.md
  explicitly warns success on one transport doesn't prove the others work.
- Security-sensitive design decisions (sandbox execution boundaries,
  region-guarding, credential/secret handling) where a wrong tradeoff has real
  consequences, not just a failed test.
- Product/pricing/onboarding decisions per AGENTS.md's "HackerAI Product
  Direction" section — these are judgment calls (solo-user activation tradeoffs),
  not implementation tasks.
- Driving a PR through review: triaging CodeRabbit/reviewer comments as
  valid-vs-not, per the repo's own PR review workflow instructions.

### Lean Codex: well-scoped, mechanical, or bulk-parallel work

Rationale: these are exactly the shape Codex's cited strengths target — cheap,
fast, sandboxed, and (via subagents) parallelizable, with a clear diff to
review at the end rather than an open-ended judgment call.

- A single leaf Convex function (e.g. `notes.ts`, `feedback.ts`) with no
  schema change.
- A single React/UI component under `components/` or `app/components/`.
- Adding or fixing one tool in `lib/ai/tools/` — each is already isolated as
  its own file + test, a repeated pattern well-suited to delegation.
- Bulk/repetitive work across many independent files: lint/type-error sweeps,
  adding missing tests that follow an existing pattern across the 125 tool
  files or 40 subagent files, individual `third_party/strix-skills/` JSON
  content updates, individual `e2e/` page-object additions.
- `scripts/` maintenance tasks.
- Any task explicit enough to hand off with a clear goal and walk away from —
  Codex's `/goal` mode is built for exactly that, and its lower per-task cost
  matters more when there's no ambiguity to resolve.

### Ambiguous — use judgment per instance

- `convex/chats.ts` / `convex/messages.ts` (2,300+ lines each): a small,
  well-isolated change inside them is Codex-shaped; a change that touches
  their interaction with billing, rate-limiting, or the streaming boundary is
  Claude-shaped.
- Test-writing in general: mechanical coverage-filling leans Codex; tests that
  encode a subtle invariant or regression fix lean Claude (the one who found
  or fixed the bug should usually write the test).

## Caveat

This is a starting heuristic based on today's tool landscape and today's
(noisy, self-reported) benchmark claims — both change fast. Re-derive rather
than trust this file once either model's capabilities materially shift.
