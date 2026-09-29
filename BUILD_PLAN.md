# hackeraiETC — phased build plan

**This repo has no build phases of its own.** It is a Next.js/Convex web
app fork that builds and deploys independently of Victorious93's Android
fleet (see `ARCHITECTURE.md`) — nothing here produces an APK, and no
change to this repo's own code is currently scheduled. This file exists
so every fleet repo has one, per the owner's standing rule, and to make
explicit *why* this one differs from `VictorSuite`'s or
`Pentest-Swarm-AI`'s: this repo is a **source**, not a **build target**.
The actual phased plan this repo feeds into lives in
`Victorious93/droidcommand-AI` (its Consumer Product Roadmap + the
`docs/AUDIT_2026-09-05.md` addenda). What follows is that plan's status
specifically as it pertains to material sourced from here — see
`droidcommand-AI/CLAUDE.md` and `docs/AUDIT_2026-09-05.md` for the
authoritative, complete version.

## Completed (in droidcommand-AI, not here)

**Phase 3 of 7 — `core-hackerai` module** (2026-09-28, droidcommand-AI).
Ported 7 of 8 audited mechanisms from this repo's `lib/ai/subagents/` as
pure-JVM Kotlin. 35 tests pass. See `ARCHITECTURE.md` for the file-level
mapping and `CODEX.md` for the corrected status history.

**Dependencies:** `docs/HACKERAI_SOURCE_AUDIT.md`'s classification
(completed 2026-09-26) of what's portable. No dependency on this repo's
own code changing — it's a read source, not a build input.

**Acceptance criteria (already met):** `./gradlew :core-hackerai:test`
passes (35/35) in droidcommand-AI; no code in this repo (hackeraiETC)
required modification.

## Next phase — `core-companion` (Phase 4 of 7, in droidcommand-AI)

**Depends on:** an Android SDK/emulator or device (not available in most
JVM-only build environments this project runs in, including this
session) — the same constraint documented in droidcommand-AI's own
`CLAUDE.md` for its `:app` module.

**Work:** an AIDL-based companion-service binding so droidcommand-AI's
`core-hackerai.HackerAiTool` (currently a stub returning
`ToolResult.Failure`) can actually invoke a running HackerAI-derived agent
process, instead of always failing.

**Acceptance criteria:**
- `HackerAiTool` returns a real result from an actual AIDL call in at
  least one integration test (mocked binder acceptable if no device is
  available; a real device/emulator run is the stronger bar and should be
  attempted when one is available).
- Existing 35 `core-hackerai` tests still pass unmodified.
- The binding's failure modes (companion not installed, service not
  bound, AIDL version mismatch) are handled explicitly, not left to throw.

**Not this repo's work to do** — it is scoped, tracked, and implemented
entirely inside `droidcommand-AI`. This file records it here only so a
future session reading this fork's docs understands what "next" means for
the fleet as a whole.

## Deliberately not scheduled

- **Multi-agent delegation** (`create_agent`/`delegate_task`/etc.) — the
  one audited mechanism from this repo that has **not** been ported.
  Needs its own design/scoping pass in droidcommand-AI first; not gated
  on `core-companion` above, but also not committed to any phase yet.
- **Any change to this repo's own code.** hackeraiETC remains a read-only
  source for the port. Re-auditing unaudited areas (chat UI, billing,
  Convex schema, most of `lib/ai/tools/`) is optional future work, not a
  scheduled phase.
