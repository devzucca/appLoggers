# Tag Decision Matrix

Tag is usually required when:

1. `sdk/` implementation changed and published artifacts must move to a new version.
2. Public API changed.
3. Integration setup changed in a way consumers need through a new published version.
4. Release workflow intentionally publishes a new milestone.

Tag is usually not required when:

1. Only documentation changed.
2. Only `.github/` workflow or agent files changed.
3. Only internal process or repo maintenance changed.
4. Only Dependabot updated CI tooling and no release milestone is intended.

Borderline cases:

1. Publishing config changed: tag only if you intentionally want a new releasable artifact.
2. Docs + code changed: decide based on whether shipped SDK behavior changed.
3. Refactor-only code change: tag only if published artifacts or runtime behavior changed, or if an intentional release was requested.

Decision rule:

1. Merge to `main` is about integration.
2. Tagging is about releasing a version.
3. Do not conflate the two.
