# Contributing to Chess Puzzles

An Android chess **puzzle** game — Kotlin + Jetpack Compose, multi-module (`:core` pure-JVM
logic + `:app` Android UI), built and run entirely from the CLI.

## Local Setup

1. Install **JDK 17** (Temurin) and the **Android SDK** command-line tools — no Android Studio
   required. [`docs/SETUP.md`](docs/SETUP.md) has the exact toolchain and pinned versions.
2. Record `JAVA_HOME`, `ANDROID_HOME`, and `PATH` in `~/.chess-env.sh` and `source` it.
3. The Gradle wrapper (8.9) is committed — `./gradlew` bootstraps everything else.

## Install Git Hooks

Run once after cloning so the local gates are active:

```
./scripts/install-hooks.sh
```

This points `core.hooksPath` at `.githooks` (per-clone config, not committed), enabling:
- **pre-commit** — fast lint (`./gradlew lintDebug`)
- **commit-msg** — Conventional Commits enforcement
- **pre-push** — owner-lock + protected-branch guard + the full test/coverage gate

`git push --no-verify` bypasses the pre-push hook; never use it to skip a failing gate.

## Build, Test, Lint

```
./gradlew :core:test                 # pure chess/puzzle engine (JUnit5)
./gradlew :app:testDebugUnitTest     # ViewModel + Robolectric Compose UI tests
./gradlew koverVerify                # 100% line-coverage gate
./gradlew lintDebug                  # Android lint
./gradlew buildSmoke                 # build + tests + coverage + lint (CI smoke)
```

## Coding Style

- **200-line maximum per file** (`.kt` / `.kts` / scripts). Split by responsibility near the limit.
- Models are immutable; `:core` logic is pure and fully unit-tested.
- 100% line coverage on all non-`@Composable` code (enforced by Kover).
- No hardcoded user-facing strings where a resource or constant fits.
- See [`CLAUDE.md`](CLAUDE.md) for the full engineering rules.

## Local Git Setup

Run once after cloning:

```bash
git config pull.rebase true          # rebase on pull instead of a merge commit
git config core.autocrlf input       # normalize CRLF -> LF on commit (macOS/Linux)
git config push.autoSetupRemote true # push without -u the first time
```

Windows contributors: use `core.autocrlf true`.

## Branch Naming

Kebab-case; the prefix matches the Conventional Commit type used in the PR:

| Prefix | Commit type | Example |
|---|---|---|
| `feature/` | `feat:` | `feature/add-hint-button` |
| `fix/` | `fix:` | `fix/drag-off-board-crash` |
| `chore/` | `chore:` | `chore/bump-dependencies` |
| `docs/` | `docs:` | `docs/clarify-setup` |
| `refactor/` | `refactor:` | `refactor/extract-board-geometry` |
| `ci/` | `ci:` | `ci/add-dependabot` |

Never commit directly to `main` — always open a PR.

## PR Checklist

- [ ] `./gradlew buildSmoke` passes (build + tests + 100% coverage + lint).
- [ ] New or changed logic ships with tests.
- [ ] No file exceeds 200 lines.
- [ ] Docs updated if behaviour changed.
- [ ] Commit messages follow Conventional Commits.
