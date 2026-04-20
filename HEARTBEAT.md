# HEARTBEAT.md — Testified-OSS improvement (random repo + worktrees + issues)

**Workspace root:** `/Users/luucrew/.openclaw/workspaces/testified-oss-coder`

**Git worktrees:** Before any `git worktree`, mirror, or teardown command, read **`tools/git-worktrees.md`** (workspace-local; cron sandboxes cannot read `~/.claude/skills/...`).

## A — Install playbooks

- `TOOLS.md` — startup order
- `tools/conventions.md`
- `tools/target-repos.md`
- `tools/git-worktrees.md` — mirror paths, `git -C`, `git/wt/` layout, push/teardown (**required** before worktree commands)
- `tools/github-issues.md`
- `tools/github-prs.md`
- `tools/scan-paths.md`
- `tools/git-worktrees.md` — mirror paths, `git -C`, `git/wt/` layout, push/teardown (**required** before worktree commands)
- **coding-agent** skill — use for every coding task (implementation, refactors, multi-file fixes, PR iteration). Do not hand-edit production fixes in place of the skill unless the skill is unavailable; if unavailable, log to `memory/YYYY-MM-DD.md` and stop or do read-only triage only.

## B — Auth

```bash
gh auth status
```
On failure: log to `memory/YYYY-MM-DD.md` and stop.

## C — Random repo (exactly one)

Pick **`OWNER/REPO`** uniformly at random from `tools/target-repos.md` (ignore `#` comments and blank lines). From workspace root:

```bash
REPO="$(grep -v '^#' tools/target-repos.md | grep -v '^[[:space:]]*$' | shuf -n1)"
export REPO
```
Every `gh` call must use `--repo "$REPO"`.

## D — Mirror + default-branch worktree (always)

Full commands, path pitfalls, and teardown: **`tools/git-worktrees.md`**.

Summary (from workspace root; `SLUG="${REPO////__}"`, mirror `git/mirrors/${SLUG}.git`):

- Ensure mirror: `git clone --mirror "https://github.com/${REPO}.git" "git/mirrors/${SLUG}.git"` if missing, else `git -C "git/mirrors/${SLUG}.git" fetch origin --prune`.
- Resolve default branch: `DEF_BRANCH="$(gh repo view "$REPO" --json defaultBranchRef -q .defaultBranchRef.name)"`.
- Add scan checkout: `git -C "git/mirrors/${SLUG}.git" worktree add "../wt/${SLUG}-${DEF_BRANCH}-scan" "$DEF_BRANCH"` (linked tree at `git/wt/${SLUG}-${DEF_BRANCH}-scan`).
- All file reads use that worktree path — **not** paths nested under the bare `.git` dir from a mistaken relative `git/wt/...` add.

## E — Open issues? (existence probe, `-L 1`)

```bash
has_open_issues=$(gh issue list --repo "$REPO" --state open -L 1 --json number --jq 'length')
```
Hard gate: probe must succeed; log raw command + output to `memory/YYYY-MM-DD.md`.

- If **`has_open_issues` is `0`** → continue to **E.2**
- If **non‑zero** → continue to **E.2** (routing decided after both probes)

## E.2 — Open PRs? (existence probe, `-L 1`)

```bash
has_open_prs=$(gh pr list --repo "$REPO" --state open -L 1 --json number --jq 'length')
```
Hard gate: probe must succeed; log to `memory/YYYY-MM-DD.md`.

## E.3 — Routing (hard gate)

| `has_open_issues` | `has_open_prs` | Route |
|-------------------|----------------|-------|
| any | **≠ 0** | **Section J** |
| **≠ 0** | **0** | **Section G** |
| **0** | **0** | **Section F** |

Do not mix routes in one run. When `has_open_prs != 0`, never run `gh issue create` or `gh pr create`. When `has_open_prs = 0` and `has_open_issues != 0`, do not run `gh pr create` directly — create a topic branch and draft PR per Section G.

## F — Create new issue (`run_path=create`)

**Precondition:** both probes returned `0`. If not, abort to J or G.

**Preflight (repeat probes):** re-run E and E.2; if issues/PRs appear, abort to J or G.

Steps:
1. Scan files per `scan-paths.md` in the worktree.
2. Dedupe per `github-issues.md`.
3. Scan for existing open issues and add a comment with additions if a relevant open issue already exists.
4. **coding-agent** skill: review improvements and generate tasks for the issue body.
5. `gh issue create --repo "$REPO" --title "..." --template "..." --body-file scratch/issue-body-OWNER-REPO.md`
6. **coding-agent** skill: suggested commit lines must match `conventions.md`.

## G — Work on existing issue (`run_path=work`)

**Precondition:** `has_open_issues != 0` and **`has_open_prs = 0`** (open issue(s), **no** open PR — you are adding the first fix branch + draft PR).

1. `gh issue list --repo "$REPO" --state open --json number,title,labels -L 30` — pick one issue `N`.
2. `gh issue view N --repo "$REPO"` (use `--json` if `gh issue view` fails on GraphQL fields; avoid relying on issue body alone for scope).
3. Add a **second worktree** on a **new topic branch** from the mirror (not `main` only): **`tools/git-worktrees.md`** + **`tools/conventions.md`** for branch name.
4. **coding-agent** skill: implement the full fix in that worktree (multi-file as needed). Do not ship a single cosmetic line as the whole "fix" unless the issue truly requires only that.
5. **Commit** on the topic branch with conventional commits (`tools/conventions.md`).
6. **Push** then **`gh pr create --draft`** per `tools/github-prs.md` (mirror push URL pattern).
7. **`gh issue comment N --repo "$REPO"`** with the draft PR link and a short summary of what changed.

**G completion gate:** log `work_outcome=draft_pr_opened` with PR URL and branch name, or `work_outcome=blocked` with failing command verbatim.

## J — Work on existing PR (`run_path=pr_followup`)

**Precondition:** `has_open_prs != 0` (includes **open issue(s) + open PR(s)** — work the existing PR branch until CI is satisfied; do not open a second PR from Section G).

1. Pick one open PR via `tools/github-prs.md` and set `P` and `HEAD`. If **`has_open_issues != 0`**, prefer a PR linked to an open issue (body/title `Fixes #N` / `Closes #N`, or `gh pr list --repo "$REPO" --search` for the issue number) when unambiguous; otherwise use the doc's draft-first / smallest-number rule.
2. Build PR worktree from mirror on `HEAD` (see `tools/github-prs.md` Section J).
3. **Review the actual branch** — not only issue/PR description or commit subject lines:
   - `gh pr diff "$P" --repo "$REPO"` and/or `git diff "origin/${BASE}...HEAD"` in the PR worktree (set `BASE` from `gh pr view` `baseRefName`).
   - Confirm changed files match the stated fix (completeness vs issue or PR scope).
4. Inspect CI (`gh pr checks`, `gh pr view --json statusCheckRollup`) and review / inline comments.
5. **Iterate until done:** while any check is **failure/error** or review requests actionable changes, use **coding-agent** skill to implement fixes in the PR worktree, then commit, push to **`HEAD`**, re-run checks. Repeat until **`gh pr checks`** shows no failures (pending/queued is OK — see outcome below).
6. If there is no actionable feedback and all required checks pass:
   - do not create empty commits,
   - comment-only once with triage summary is allowed.

**Mandatory completion gate for Section J:**
- A run is not complete until one of these is true and logged:
  - `pr_followup_outcome=fixed` with commit SHA(s), push output, and note that **`gh pr checks`** has **no failing** checks (or link to green run), or
  - `pr_followup_outcome=waiting_on_ci` with evidence that failures are **none** and remaining items are pending only, or
  - `pr_followup_outcome=no_actionable_feedback` with links to checks/comments **and** confirmation that **`gh pr diff`** was reviewed (not "read title only").
- Never claim PR work was done without either commits pushed or explicit no-actionable evidence plus diff review.

## H — Teardown

Use **`git -C "$MIRROR" worktree remove --force "../wt/<same-relative-path-used-at-add>"`** for each linked tree (see **`tools/git-worktrees.md`**). Keep bare mirrors. Append `memory/YYYY-MM-DD.md`.

## I — Final Discord message

Full summary: repo, run_path, probe values, worktree paths, issue/PR links, checks summary (including **no failing** checks for J), for **J** note that **`gh pr diff` / branch diff was reviewed**, and explicit `*_outcome*` (`create|work|pr_followup` plus `work_outcome` / `pr_followup_outcome` where applicable). Send to channel **1069257061533233182**.

--- Run completed at $(date) ---

## Current Run Status

**Repository**: tested-oss/pytest-api-testing  
**Run Path**: J (work on existing PR)  
**Probe Values**: issues=1, prs=1  
**Worktree Path**: git/mirrors/wt/testified-oss__pytest-api-testing-fix-15  
**Issue Links**: https://github.com/tested-oss/pytest-api-testing/issues/15  
**PR Links**: https://github.com/tested-oss/pytest-api-testing/pull/17  
**Checks Summary**: no checks reported on the 'fix/issue-15-verify-pytest-config' branch  
**PR Followup Outcome**: waiting_on_ci  
**Work Outcome**: pr_followup_outcome=waiting_on_ci (no failing checks, branch is ahead of main with one commit)  
**Teardown**: Worktree preserved for future runs per git-worktrees.md  
**Memory Log**: memory/2026-04-21.md  
**Discord Notification**: Ready

## Run completed at 2026-04-21 06:20:43 UTC
