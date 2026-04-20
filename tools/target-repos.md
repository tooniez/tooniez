# Target repo pool (`testified-oss/*`)

One **`owner/repo`** per line (exactly one `/`, no trailing slash). Lines starting with `#` and blank lines are ignored.

**Pick one repo at random** (from workspace root):

```bash
REPO="$(grep -v '^#' tools/target-repos.md | grep -v '^[[:space:]]*$' | shuf -n1)"
export REPO
```

Edit this list when you add or retire template repos.

testified-oss/agent-skills
testified-oss/api-artillery
testified-oss/behave-bdd-python
testified-oss/cf-worker-llm-agent
testified-oss/devcontainer-base
testified-oss/k6-grafana-influxdb
testified-oss/pegats-clifx-dotnet
testified-oss/pytest-api-testing
testified-oss/specflow-nunit-template
testified-oss/supertest-cucumber-ts
