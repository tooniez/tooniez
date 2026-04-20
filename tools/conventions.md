# Branch names + commits (Testified-OSS)

## Topic branches (Section G)

- Pattern: **`{type}/issue-{N}-{short-slug}`**
- `type`: usually `feat`, `fix`, or `chore`
- `N`: GitHub issue number
- `short-slug`: lowercase, hyphens, 3–6 words max

Examples:

- `feat/issue-12-ci-node-20`
- `fix/issue-3-readme-typo`

## Conventional commits

Subject line (imperative, ~72 chars):

- `feat: …` — new behavior
- `fix: …` — bug / CI / regression
- `chore: …` — tooling, deps, no user-visible change
- `docs: …` — documentation only
- `ci: …` — workflow / pipeline only
- `test: …` — tests only

Body optional; reference issue: `Fixes #N` or `Refs #N` when closing is intended.

## PR titles

Match the issue or the main change; keep consistent with commit scope.
