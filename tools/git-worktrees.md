# Git mirrors + worktrees (Testified-OSS workspace)

All paths are relative to the **workspace root** (the directory that contains `git/mirrors/` and `git/wt/`). **Run every block from that directory** (`cd` there first). **`REPO`** must already be set (`owner/name`, lowercase, exactly one `/`).

---

## 1 — `SLUG` and `MIRROR` (must match GitHub layout)

Use **exactly** this substitution (replaces `/` with `__`):

```bash
SLUG="${REPO//\//__}"
MIRROR="git/mirrors/${SLUG}.git"
```

**Never** use `${REPO////__}` (four slashes) — it corrupts `SLUG` and breaks every following `git` command.

Sanity check (optional but catches typos before any clone):

```bash
[[ "$REPO" == */* ]] && [[ "$REPO" != /* ]] && [[ "$SLUG" == *__* ]]
```

---

## 2 — D: mirror + default-branch scan worktree (idempotent, zero-error path)

Copy this block **verbatim** after `REPO` is set. It uses **only** `git -C "$MIRROR"` and **`../wt/...`** so checkouts always land under **`git/wt/`**, never under `git/mirrors/wt/`.

```bash
mkdir -p git/mirrors git/wt

SLUG="${REPO//\//__}"
MIRROR="git/mirrors/${SLUG}.git"

if [[ ! -d "$MIRROR" ]]; then
  git clone --mirror "https://github.com/${REPO}.git" "$MIRROR"
else
  git -C "$MIRROR" fetch origin --prune
fi

# Drop stale worktree metadata so "already registered" / ghost paths do not confuse the next add
git -C "$MIRROR" worktree prune

DEF_BRANCH="$(gh repo view "$REPO" --json defaultBranchRef -q .defaultBranchRef.name)"
[[ -n "${DEF_BRANCH}" ]] || exit 1

WT_NAME="${SLUG}-${DEF_BRANCH}-scan"
WT_SCAN="git/wt/${WT_NAME}"
WT_REL="../wt/${WT_NAME}"

# If this scan tree is already linked to this mirror, do nothing (success).
if git -C "$MIRROR" worktree list | grep -F "${WT_NAME}" >/dev/null 2>&1; then
  :
else
  # Path on disk but not linked: remove so `worktree add` cannot fail with "already exists"
  if [[ -e "$WT_SCAN" ]]; then
    [[ "$WT_SCAN" == git/wt/* ]] || exit 1
    rm -rf "$WT_SCAN"
  fi
  git -C "$MIRROR" worktree add "$WT_REL" "$DEF_BRANCH"
fi

export REPO SLUG MIRROR DEF_BRANCH WT_SCAN WT_REL
```

After this block: **`test -d "$WT_SCAN"`** must be true; **`git -C "$MIRROR" worktree list`** must list a line ending with **`/${WT_NAME}`** (absolute path on your machine).

---

## 3 — Section G: topic branch (second worktree)

Replace `ISSUE` and `BR_TOPIC` (branch name per `tools/conventions.md`):

```bash
ISSUE=123
BR_TOPIC="feat/issue-${ISSUE}-short-slug"
WT_TOPIC_NAME="${SLUG}-issue-${ISSUE}"
WT_TOPIC_REL="../wt/${WT_TOPIC_NAME}"
WT_TOPIC_SCAN="git/wt/${WT_TOPIC_NAME}"

git -C "$MIRROR" fetch origin "$DEF_BRANCH"
git -C "$MIRROR" worktree prune

if git -C "$MIRROR" worktree list | grep -F "${WT_TOPIC_NAME}" >/dev/null 2>&1; then
  :
else
  if [[ -e "$WT_TOPIC_SCAN" ]]; then
    [[ "$WT_TOPIC_SCAN" == git/wt/* ]] || exit 1
    rm -rf "$WT_TOPIC_SCAN"
  fi
  git -C "$MIRROR" worktree add -b "$BR_TOPIC" "$WT_TOPIC_REL" "$DEF_BRANCH"
fi
```

---

## 4 — Section J: PR head worktree

Set **`P`** (PR number) and **`HEAD`** (`headRefName` from `gh pr view`). Then:

```bash
git -C "$MIRROR" fetch origin "$HEAD"
git -C "$MIRROR" worktree prune

WT_PR_NAME="${SLUG}-pr-${P}"
WT_PR_REL="../wt/${WT_PR_NAME}"
WT_PR_PATH="git/wt/${WT_PR_NAME}"

if git -C "$MIRROR" worktree list | grep -F "${WT_PR_NAME}" >/dev/null 2>&1; then
  :
else
  if [[ -e "$WT_PR_PATH" ]]; then
    [[ "$WT_PR_PATH" == git/wt/* ]] || exit 1
    rm -rf "$WT_PR_PATH"
  fi
  git -C "$MIRROR" worktree add "$WT_PR_REL" "$HEAD"
fi
```

---

## 5 — Push (mirror remotes cannot use normal `git push origin`)

Run **inside** the worktree that has commits:

```bash
BRANCH="$(git branch --show-current)"
git push "https://github.com/${REPO}.git" "HEAD:refs/heads/${BRANCH}"
```

---

## 6 — Teardown (Section H)

Paths to remove are the **`../wt/...`** names **as shown by**:

```bash
git -C "$MIRROR" worktree list
```

For each linked worktree (not the `(bare)` line):

```bash
git -C "$MIRROR" worktree remove --force "../wt/${WT_NAME}"
```

Use the exact `../wt/<same-suffix-as-add>` you used at `worktree add`. If remove fails, run **`git -C "$MIRROR" worktree prune`**, list again, retry once.

---

## 7 — If anything still fails (last resort)

1. Re-print `REPO`, `SLUG`, `MIRROR`, `DEF_BRANCH`, `WT_REL` and confirm **`SLUG`** equals **`testified-oss__behave-bdd-python`** style (two segments, `__` between owner and repo).
2. Run **`git -C "$MIRROR" worktree list`** and align paths with **`git/wt/`** only.
3. Do **not** invent alternate mirror directories; do not run `git worktree` from the workspace root without **`-C "$MIRROR"`**.
