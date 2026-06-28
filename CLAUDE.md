# CLAUDE.md

Project: Android chess **puzzle** game — Kotlin + Jetpack Compose, multi-module (`:core` pure-JVM
logic + `:app` Android UI). Built and run entirely from the CLI. See `docs/PLAN.md` for the full
architecture and build plan.

## Binding engineering rules

Non-negotiable. Apply to every change.

### 1. 200-line file cap
No code file may exceed **200 lines** (total, including blanks and comments). Applies to every
`.kt`, `.kts`, and script file. When a file approaches the limit, split it by responsibility into
smaller files (extract composables, mappers, helpers, etc.). Enforced by a file-length check in the
pre-push hook.

### 2. `/simplify` to a fixpoint before every commit
Before each commit, run `/loop /simplify` over the working changes and keep iterating until a full
pass changes **zero lines**. Commit only once `/simplify` is stable.

### 3. `/code-review` to a fixpoint before every commit
Before each commit, run `/loop /code-review --fix` and keep iterating until a full pass changes
**zero lines**. Commit only once the review is stable.
- Run rules 2 and 3 together until **both** are simultaneously stable — each can create new work for
  the other.
- Note: `/review` is for GitHub PRs; for the local working diff use `/code-review`.

### 4. Pre-push test hook
A git `pre-push` hook MUST run the full test suite (`./gradlew test`) **and** the coverage gate, and
must block the push on any failure. Bypassing it (`git push --no-verify`) is forbidden.

### 5. 100% test coverage
Maintain **literal 100%** line/branch coverage across **all** code — `:core`, `:app` logic
(ViewModels, `data`, `util`), **and** Compose UI + entry points (`MainActivity`, `Application`).
Enforced by a Kover verification gate (the build fails below 100%) wired into the build and the
pre-push hook. Every change ships with its tests.
- UI/entry-point coverage is achieved **headlessly on the JVM** via Robolectric + Compose UI tests
  (`createComposeRule`, kept in the `test` source set so Kover counts them) and Roborazzi
  (`@GraphicsMode(NATIVE)`) to execute the custom Canvas drawing. No device/emulator is needed for
  the coverage gate; the emulator screenshot remains the end-to-end "it runs" proof.

## Per-change workflow
write code + tests → `/loop /simplify` (to fixpoint) → `/loop /code-review --fix` (to fixpoint) →
`./gradlew test koverVerify` green → commit → push (pre-push hook re-runs tests + coverage).
