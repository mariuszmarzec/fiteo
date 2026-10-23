#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

OPENAPI_FILE="src/jvmMain/resources/openapi.yaml"
PROMPT_FILE="scripts/prompts/update-api-docs.md"
VERIFIER_FILE="scripts/verify-api-docs.py"

command -v opencode >/dev/null 2>&1 || {
  echo "ERROR: opencode is not installed or not on PATH." >&2
  exit 1
}

command -v python3 >/dev/null 2>&1 || {
  echo "ERROR: python3 is required to verify the generated API documentation." >&2
  exit 1
}

[[ -f "$PROMPT_FILE" ]] || {
  echo "ERROR: prompt file not found: $PROMPT_FILE" >&2
  exit 1
}

[[ -f "$VERIFIER_FILE" ]] || {
  echo "ERROR: API documentation verifier not found: $VERIFIER_FILE" >&2
  exit 1
}

echo "==> Generating OpenAPI snapshot from the Ktor routing tree..."
./gradlew generateOpenApiSnapshot

[[ -s "$OPENAPI_FILE" ]] || {
  echo "ERROR: generated OpenAPI file is missing or empty: $OPENAPI_FILE" >&2
  exit 1
}

echo "==> Updating Markdown API documentation with OpenCode..."
opencode run --agent build "$(cat "$PROMPT_FILE")"

echo "==> Verifying runtime-critical API documentation invariants..."
python3 "$VERIFIER_FILE"

echo
echo "==> API documentation synchronization and verification finished."
echo "Review the result with:"
echo "  git diff -- docs/api"
echo "  git status --short -- docs/api"
