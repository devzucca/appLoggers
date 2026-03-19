# Release Sequence

Correct order:

1. Complete work locally.
2. Push to `dev`.
3. Open PR `dev -> main`.
4. Merge after green checks.
5. Update local `main`.
6. Decide whether the merged change is tag-eligible.
7. Create annotated tag from `main` only if tag-eligible.
8. Push tag.

Typical commands:

1. `git checkout main`
2. `git pull origin main`
3. `git tag -a vX.Y.Z -m "Release X.Y.Z"`
4. `git push origin vX.Y.Z`

Docs-only example:

1. complete docs change
2. push to `dev`
3. open PR `dev -> main`
4. merge after green checks
5. stop there, no tag
