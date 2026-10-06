#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

OPENAPI_FILE="src/jvmMain/resources/openapi.yaml"
PROMPT_FILE="scripts/prompts/update-api-docs.md"

command -v opencode >/dev/null 2>&1 || {
  echo "ERROR: opencode is not installed or not on PATH." >&2
  exit 1
}

[[ -f "$PROMPT_FILE" ]] || {
  echo "ERROR: prompt file not found: $PROMPT_FILE" >&2
  exit 1
}

echo "==> Generating OpenAPI snapshot..."
./gradlew generateOpenApiSnapshot

[[ -s "$OPENAPI_FILE" ]] || {
  echo "ERROR: generated OpenAPI file is missing or empty: $OPENAPI_FILE" >&2
  exit 1
}

echo "==> Updating Markdown API documentation with OpenCode..."
opencode run "$(cat "$PROMPT_FILE")"

echo "==> API documentation update finished."
echo "Review the changes with: git diff -- docs/api"
