# GitHub issues (`gh`)

Always pass **`--repo "$REPO"`** (or `GH_REPO` if you set it consistently).

## View / list (avoid GraphQL-only failures)

```bash
gh issue list --repo "$REPO" --state open --json number,title,labels -L 50
```

If `gh issue view N` errors on projects/classic GraphQL, use JSON fields only:

```bash
gh issue view N --repo "$REPO" --json number,title,body,labels,state,url
```

## Before creating a new issue (Section F)

1. Search open issues for the same theme (title keywords, labels).
2. If one fits, **`gh issue comment`** with new findings instead of duplicating.
3. Otherwise create with a body file under `scratch/` (see `HEARTBEAT.md` Section F).

## Create

```bash
gh issue create --repo "$REPO" --title "…" --body-file scratch/issue-body-…md
```

Suggested future commit lines in the body must follow **`tools/conventions.md`**.

## Comment (Section G)

```bash
gh issue comment N --repo "$REPO" --body "…"
```

Include draft PR link when opening a fix PR.
