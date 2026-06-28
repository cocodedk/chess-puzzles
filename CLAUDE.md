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
Maintain **100% line coverage** across all non-`@Composable` code — `:core`, and `:app` logic
(ViewModels, `data`, `util`, board geometry/rendering helpers). Enforced by the Kover `koverVerify`
gate (the build fails below 100%) wired into the build and the pre-push hook. Every change ships
with its tests.
- The custom Canvas draw code is covered **headlessly on the JVM** by Robolectric Compose tests that
  draw the hosted view to a software `Canvas` under `@GraphicsMode(NATIVE)` (see
  `TestSupport.renderToBitmap`).
- **Branch** coverage is very high but **not** gate-enforced: idiomatic Kotlin inline/synthetic
  constructs (`MutableStateFlow.update`'s CAS retry, `Iterable.all`/`filter` internals, …) emit
  branch stubs unreachable by single-threaded tests.
- **`@Composable`** functions are excluded from the metric (Compose-compiler recomposition branches
  are unreachable by any test); they are still exercised by the Robolectric render tests and the
  emulator screenshot.

## Per-change workflow
write code + tests → `/loop /simplify` (to fixpoint) → `/loop /code-review --fix` (to fixpoint) →
`./gradlew test koverVerify` green → commit → push (pre-push hook re-runs tests + coverage).
