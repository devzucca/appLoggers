# Changelog

All notable changes to AppLogger are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) and the project adheres to [Semantic Versioning](https://semver.org/).

---

## [Unreleased]

### Added
- **CLI project profiles**: `APPLOGGER_CONFIG` and `--config` now support a shared JSON config file for multiple telemetry apps/projects.
- **Project selection controls**: `APPLOGGER_PROJECT` and `--project` allow explicit project selection when multiple Supabase backends exist.
- **Workspace autodetection**: the CLI can resolve the active telemetry project from the current working directory when `workspace_roots` are configured.
- **Machine-readable project context**: `health` and telemetry JSON/agent responses now include the resolved `project` and `config_source` when available.
- **Corporate-ready distribution pipeline**: CLI release workflow now contains conditional publication jobs for Homebrew, Scoop, and Winget repositories.

### Changed
- **CLI CI gate hardening**: multi-OS CLI tests now fail the pipeline on every protected runner instead of tolerating macOS or Windows failures.
- **Release metadata embedding**: CLI binaries now embed version, commit, and build date from valid linker targets and are verified in a governance gate before release.
- **Security scanner reproducibility**: `gosec` is installed from a pinned immutable release instead of a mutable branch reference.
- **Supabase error guidance**: CLI configuration errors now mention `APPLOGGER_SUPABASE_URL` and `APPLOGGER_SUPABASE_KEY` aliases explicitly.
- **Repository markdown governance**: markdown quality is now enforced repository-wide through a centralized `.markdownlint.jsonc` policy aligned with the real documentation style.

### Security
- Release governance now blocks `main` and tag deliveries when embedded CLI build metadata remains at `dev`, `none`, or `unknown`.
- Package-manager publication remains gated behind explicit repository variables and secrets, reducing accidental distribution drift.

### Planned
- `logger-transport-firebase` module — transport to Firebase Realtime Database
- Support for `logger-transport-grpc` — direct delivery via gRPC to a custom server
- Wear OS support in `PlatformDetector`
- Web dashboard for real-time log visualization

---

## [0.1.0-alpha.1] — 2026-03-17

### Added
- **`AppLogger` interface** — unified logging contract for Kotlin (Android / JVM / iOS).
- **`LogTransport` interface** — swappable transport abstraction (REST, gRPC, stdio).
- **`LogBuffer` interface** — temporary event storage with configurable overflow policy.
- **`LogFilter` interface** — event filtering with chain-of-responsibility support.
- **`LogFormatter` interface** — `LogEvent` serialization (JSON implementation included).
- **`DeviceInfoProvider` interface** — technical device metadata without PII.
- **`CrashHandler` interface** — `UncaughtException` capture with synchronous pre-death flush.
- **`AppLoggerImpl`** — core event pipeline with `Channel<LogEvent>` from Kotlin Coroutines.
- **`BatchProcessor`** — batch processor with size-based, time-based, and severity-based flush.
- **`RateLimitFilter`** — per-tag rate limiter with auto-bypass for ERROR and CRITICAL.
- **`NoOpLogger`** — empty implementation for pre-init state and tests.
- **`InMemoryLogger`** — test implementation with built-in assertions.
- **`FakeTransport`** — transport mock with success/failure control for tests.
- **`SupabaseTransport`** — Supabase (PostgreSQL) transport with `anon key` auth via Ktor.
- **`PlatformDetector`** — automatic detection of ANDROID_MOBILE, ANDROID_TV, WEAR_OS, JVM.
- **`SqliteOfflineBuffer`** — persistent FIFO buffer in SQLite for offline operation on Android TV.
- **`AppLoggerLifecycleObserver`** — automatic flush when app backgrounds.
- **`AppLoggerConfig.Builder`** — typed configuration builder with adaptive defaults per platform.
- **`AppLoggerSDK`** — public entry singleton (Android), idempotent initialization.
- **`AppLoggerIos`** — public iOS entry singleton for Kotlin Multiplatform (`iosMain`).
- **`logger-test` module** — testing utilities: `NoOpTestLogger`, `InMemoryLogger`, `FakeTransport`.
- **Privacy by design**: no PII captured, ephemeral `session_id`, optional `user_id` with consent.
- **Crash handler chaining**: SDK chains the previous handler, never replaces it.
- **Mandatory TLS**: builder rejects HTTP endpoints in production mode.
- **Complete documentation**: `docs/ES/desarrollo/`, `docs/ES/paquete/`.
- **SQL migrations**: `docs/ES/migraciones/001` a `docs/ES/migraciones/005` for PostgreSQL / Supabase.
- **CI/CD with GitHub Actions**: test workflows on PRs and automated release on tags.
- **Monorepo structure**: `sdk/`, `docs/ES/`, `docs/EN/`, `frontend/`, `cli/`.
- **JitPack publication**: all 3 modules with 6 KMP platform variants.
- **GitHub Packages publication**: automated via `release.yml` workflow on `v*` tags.
- **Professional README**: configuration guide, CI/CD docs, branching model.
- **`local.properties.example`**: onboarding template for new contributors.

### Security
- API keys never hardcoded: injected via `BuildConfig` from `local.properties` or CI env vars.
- Row Level Security in Supabase: `anon` role only has `INSERT` permission on `app_logs`.
- Production endpoint requires `https://` — validated at `Config` build time.
- **CodeQL security scanning** in CI workflow.
- **Dependency submission** to GitHub for vulnerability alerts.
