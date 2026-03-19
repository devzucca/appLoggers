---
name: applogger-kmp-integration
description: Use this skill when integrating AppLogger in Android or iOS with Kotlin Multiplatform, validating setup, initialization, health checks, and troubleshooting delivery issues.
---

# AppLogger KMP Integration

## When to use this skill

Use this skill when the user needs to:

1. Integrate AppLogger in Android or iOS from Kotlin code.
2. Configure AppLogger safely with environment variables.
3. Validate initialization and runtime health.
4. Troubleshoot event delivery, buffering, or transport errors.

Do not use this skill for:

1. Native host flows outside KMP (deprecated in this project).
2. Backend schema design unrelated to AppLogger usage.
3. Non-Kotlin integration instructions.

## Mandatory constraints

1. Prefer Kotlin Multiplatform flow using `commonMain` and `iosMain`.
2. Do not recommend hardcoding secrets.
3. Validate snippets against current SDK API before sharing.
4. Keep examples minimal and executable.

## Execution workflow

1. Identify platform target: Android, iOS, or both.
2. Confirm prerequisites and dependency coordinates.
3. Provide initialization with `AppLoggerConfig.Builder()`.
4. Provide level-based logging examples.
5. Provide `AppLoggerHealth.snapshot()` verification steps.
6. Provide a troubleshooting checklist with concrete fixes.

## References in this repository

1. `../../../docs/ES/agents/android-integration.md`
2. `../../../docs/ES/agents/ios-integration.md`
3. `../../../docs/ES/desarrollo/integration-guide.md`
4. `../../../docs/ES/desarrollo/api-compatibility.md`
5. `../../../docs/ES/paquete/architecture.md`

## Output quality standard

1. Explain required steps before optional optimizations.
2. Include assumptions explicitly when context is missing.
3. Prefer precise troubleshooting over generic advice.