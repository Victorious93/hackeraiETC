# Fork notes (Victorious93) — not part of upstream hackerai-tech/hackerai

This file is fork-specific and is **not** synced from upstream. `CLAUDE.md`
(which imports `AGENTS.md`) is upstream's own engineering guidance for
building HackerAI itself — read that first for any change to this
codebase's actual product code. This file exists only to track this fork's
role and phase in Victorious93's own project set, per that project owner's
standing rule that every actively-worked project gets a phase-tracking
file. See `ARCHITECTURE.md` for the fork-specific component/status detail
this file points to.

## Role of this repo

A maintained fork of `hackerai-tech/hackerai` (Apache 2.0, "with Commercial
Restrictions" per this repo's own README badge — check that before reusing
any of its code beyond ideas/patterns). It is **not itself** the build
target for Victorious93's companion-app project, and it does not build an
Android APK. The actual target is `Victorious93/droidcommand-AI`'s own
Kotlin modules, populated via clean-room reimplementation of select
mechanisms from this codebase's `lib/ai/subagents/` domain — never by
copying this repo's source or assets.

## Current phase

**Corrected 2026-09-29 — the previous version of this file (2026-09-27)
said port design/scoping and implementation were both "not started."
That was accurate on 2026-09-27 and is no longer accurate**: see
`ARCHITECTURE.md`'s "What has actually been ported" section for the full
detail, summarized here.

- [x] Fork exists, tracked at `Victorious93/hackeraietc`
- [x] Source audit complete — `Victorious93/droidcommand-AI`'s
      `docs/HACKERAI_SOURCE_AUDIT.md` (2026-09-26), scoped to
      `lib/ai/subagents/`/`lib/api/agent-*.ts`.
- [x] **Port design decision made and executed, 2026-09-28:** the ported
      functionality became a new module (`core-hackerai`) **inside
      droidcommand-AI**, not a genuinely separate repository — closing the
      open design question the 2026-09-27 version of this file flagged.
- [x] **Partial implementation, VERIFIED (droidcommand-AI's own grading,
      not inflated here):** `core-hackerai` ports 7 of the 8 audited
      mechanisms (contracts/schemas, doom-loop detection, runtime
      recovery, step-budget gating, delegation budget, evidence
      references, skill ranking + the 62-entry Strix skill catalog) as
      pure-JVM Kotlin, 35 passing tests, in droidcommand-AI's own repo —
      see `ARCHITECTURE.md` for the file-by-file mapping. **This fork's
      own code is unmodified** by that work; nothing here needs to change
      for it, which is why this repo's `main` has had no port-related
      commits.
- [ ] **Not ported:** the multi-agent delegation contracts themselves
      (`create_agent`/`delegate_task`/`send_message_to_agent`/
      `wait_for_agents`/`list_agents`/`cancel_agent`) — droidcommand-AI's
      agent loop is still single-agent. This was never scoped as part of
      the Phase 3 work; it remains a distinct, larger, unscoped feature
      (droidcommand-AI's own audit calls it a "Phase 6 candidate," not
      committed).
- [ ] **Blocked on Android SDK:** `HackerAiTool` in `core-hackerai` is a
      stub (`ToolResult.Failure` always) until droidcommand-AI's `Phase 4`
      (`core-companion`, an AIDL binding) exists — that module needs an
      Android SDK this environment doesn't have and hasn't been started.

## Next build phase

The next actionable phase belongs to `droidcommand-AI`, not this repo —
this fork has nothing left to build against `core-hackerai`'s current
scope. For a future session picking this back up, in dependency order:

1. **`droidcommand-AI`: `core-companion` (Phase 4)** — the AIDL companion
   binding `HackerAiTool` needs to stop being a stub. Android-SDK-gated;
   cannot start in a JVM-only environment. This is the actual next step
   and it lives in the other repo, not here.
2. **Multi-agent delegation, if the owner wants it** — needs its own
   design/scoping pass in `droidcommand-AI` first (per that repo's
   workflow rules, every roadmap phase is scoped and approved before
   implementation). Not gated on anything else here; genuinely optional
   relative to the companion-binding work above.
3. **This repo (hackeraiETC) itself has no scheduled work.** It remains a
   read-only source for the port; the only reason to touch it again is if
   the owner wants to track further upstream changes (`git fetch upstream`
   — not configured as a remote in this shallow clone, verify before
   assuming) or audit a part of the codebase this pass didn't cover (chat
   UI, billing, Convex schema, most of `lib/ai/tools/` — all explicitly
   marked not-audited in `docs/HACKERAI_SOURCE_AUDIT.md`).

## Acceptance criteria for the next phase (`core-companion`, in droidcommand-AI)

Recorded here since this fork is the thing that phase would finally make
`HackerAiTool` call into, even though the work itself happens in the
other repo:

- `droidcommand-AI`'s `:app` module (or a dedicated companion-binding
  layer) can invoke a running HackerAI-derived agent process via AIDL and
  get a real result back — not `ToolResult.Failure`.
- `HackerAiTool`'s existing 35 tests still pass, plus new tests covering
  the AIDL call path (mocked binder, since no Android SDK/device is
  available in most build environments this project runs in).
- Requires an Android SDK to even scaffold — cannot be scoped further
  from a JVM-only session; flag as blocked, don't attempt a partial
  JVM-side stand-in that would misrepresent the dependency.

## Repo history note

Most recent commit merges PR #4 (branch `claude/continue-cb56ey`). This
shallow clone doesn't show what that PR changed — not independently
verified against full history; re-check with a deeper fetch if it matters
to a future session. No repo-owned commits correspond to the
`core-hackerai` port work above, because that work lives entirely in
`droidcommand-AI` — confirmed by this repo's own unchanged history.

## Source of truth

When this file and `ARCHITECTURE.md` disagree with the actual state of
either this repo or `droidcommand-AI`, re-verify against both repos
directly — `docs/HACKERAI_SOURCE_AUDIT.md` and
`docs/AUDIT_2026-09-05.md`'s addenda in droidcommand-AI are the
authoritative status for the port; this file is a pointer into them, not
a replacement.
