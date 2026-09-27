# Fork notes (Victorious93) — not part of upstream hackerai-tech/hackerai

This file is fork-specific and is **not** synced from upstream. `CLAUDE.md`
(which imports `AGENTS.md`) is upstream's own engineering guidance for
building HackerAI itself — read that first for any change to this
codebase's actual product code. This file exists only to track this fork's
role and phase in Victorious93's own project set, per that project owner's
standing rule that every actively-worked project gets a phase-tracking file.

## Role of this repo

A maintained fork of `hackerai-tech/hackerai` (Apache 2.0, "with Commercial
Restrictions" per this repo's own README badge — check that before reusing
any of its code beyond ideas/patterns). It is **not itself** the build
target for Victorious93's stated companion-app project. The actual target —
a standalone companion Android app that ports select functionality from
this codebase (multi-agent delegation, tool/skill catalog patterns) into
`Victorious93/droidcommand-AI`'s existing `core-agent`/`core-security`/
`core-tools-android`/`core-termux`/`core-root` modules, via clean-room
reimplementation, never by copying this repo's source or assets — has no
repository of its own yet (verified: not present in this session's repo
listing as of 2026-09-27).

## Current phase

- [x] Fork exists, tracked at `Victorious93/hackeraietc`
- [x] Source audit complete — see `Victorious93/droidcommand-AI`'s
      `docs/HACKERAI_SOURCE_AUDIT.md` (dated 2026-09-26) for the
      classification of what's portable vs. what droidcommand-AI already
      has. That audit states plainly that no code changed as a result and
      nothing from it is yet scheduled into any roadmap phase.
- [ ] Port design/scoping — **not started**. No `CAP-###` item or Consumer
      Product Roadmap phase in droidcommand-AI currently schedules this
      work.
- [ ] Companion app repository — **not created**.
- [ ] Implementation — **not started**.

Note on repo history: the most recent commit here is a merge of PR #4
(branch `claude/continue-cb56ey`). This shallow clone doesn't show what that
PR changed — don't assume it relates to the port work above without
checking it directly if it matters to a future session.

## Next action

None scheduled. Before starting the port, re-read
`docs/HACKERAI_SOURCE_AUDIT.md` in droidcommand-AI (check its own dated
addenda for staleness first), then decide — as an explicit design step, not
something to assume — whether the ported functionality becomes a new
droidcommand-AI module or a genuinely separate repo, before writing any
code.
