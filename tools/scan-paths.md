# Scan paths (Section F — new issue from repo review)

Run reads from the **scan worktree** (`WT_SCAN` from `git-worktrees.md`), not the bare mirror.

## Default glob / directory order

1. **`README.md`** (or `README.rst`)
2. **`.github/workflows/*.yml`** and **`*.yaml`**
3. **`docs/**`** if present
4. Language roots (pick what exists):
   - **Node:** `package.json`, `tsconfig.json`, `eslint.config.*`, `src/`
   - **Python:** `pyproject.toml`, `requirements*.txt`, `setup.cfg`, `tests/`, `src/` or package dir
   - **.NET:** `*.csproj`, `*.sln`, `.github/workflows/`
5. **`Dockerfile`**, **`docker-compose*.yml`**
6. **`.devcontainer/**`**

## Goal

Surface actionable improvements: CI gaps, outdated pins, missing tests, docs drift, security footguns. Dedupe against open issues per **`github-issues.md`**.
