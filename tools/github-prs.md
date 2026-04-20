# GitHub PRs (`gh`)

Always pass **`--repo "$REPO"`** unless `GH_REPO` is set to `"$REPO"`.

Worktree commands for PR heads live in **`tools/git-worktrees.md`** §4 (mirror-relative `../wt/...` paths).

## Variables for Section J

After picking PR number **`P`**:

```bash
HEAD="$(gh pr view "$P" --repo "$REPO" --json headRefName -q .headRefName)"
BASE="$(gh pr view "$P" --repo "$REPO" --json baseRefName -q .baseRefName)"
```

Export `P`, `HEAD`, `BASE` for `HEARTBEAT.md` Section J (diff: `git diff "origin/${BASE}...HEAD"` in the PR worktree).

## Picking which open PR to work (tie-break)

When multiple PRs are open:

1. Prefer **draft** PRs that need CI or fixes (then ready for review).
2. If **`has_open_issues != 0`**, prefer a PR whose title/body references an open issue (`Fixes #N`, `Closes #N`, or `gh pr list --repo "$REPO" --search "N in:body"`).
3. Else prefer the **smallest PR number** that still has failing checks or open review threads.
4. If still tied, smallest **`P`**.

## CI + diff

```bash
gh pr checks "$P" --repo "$REPO"
gh pr view "$P" --repo "$REPO" --json statusCheckRollup,url,isDraft
gh pr diff "$P" --repo "$REPO"
```

## Draft PR after push (Section G)

From the topic worktree, after **`git push`** per `git-worktrees.md` §5:

```bash
gh pr create --repo "$REPO" --draft --head "$BRANCH" --title "…" --body "Fixes #N

…summary…"
```

Adjust `--head` if the default remote branch name differs; ensure the branch exists on `origin`.

## Comments

```bash
gh pr comment "$P" --repo "$REPO" --body "…"
```
