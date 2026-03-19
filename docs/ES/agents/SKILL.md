---
name: applogger-kmp-integration
description: Use this skill when integrating AppLogger in Android or iOS using Kotlin Multiplatform, validating configuration, initialization, health checks, and troubleshooting.
---

# AppLogger KMP Integration Skill

## When to use this skill

Use this skill when the user needs to:

1. Integrate AppLogger in Android or iOS from Kotlin code.
2. Configure AppLogger safely with environment variables.
3. Validate initialization and health status.
4. Troubleshoot telemetry delivery or buffer behavior.

Do not use this skill for:

1. Integración host nativa externa (deprecada en este proyecto).
2. Non-KMP setup guides.
3. Backend schema design outside AppLogger docs.

## Mandatory constraints

1. Prefer Kotlin Multiplatform flow (`commonMain` + `iosMain`).
2. Do not recommend host package-manager flows for new implementations.
3. Never suggest hardcoding secrets.
4. Verify API usage against current SDK code before giving examples.

## Execution workflow

1. Identify target platform: Android, iOS, or both.
2. Validate prerequisites and dependencies.
3. Provide initialization code with `AppLoggerConfig.Builder()`.
4. Provide usage examples for `debug/info/warn/error/critical/metric`.
5. Provide `AppLoggerHealth.snapshot()` checks.
6. Provide troubleshooting checklist.

## Canonical references in this repository

1. Android integration: `docs/ES/agents/android-integration.md`
2. iOS KMP integration: `docs/ES/agents/ios-integration.md`
3. Full integration guide: `docs/ES/desarrollo/integration-guide.md`
4. Compatibility matrix: `docs/ES/desarrollo/api-compatibility.md`
5. Architecture details: `docs/ES/paquete/architecture.md`

## Output quality standard

1. Explain steps in simple language.
2. Keep examples executable and minimal.
3. Separate required steps from optional optimizations.
4. Include common errors and exact fixes.
5. State assumptions explicitly when information is missing.
