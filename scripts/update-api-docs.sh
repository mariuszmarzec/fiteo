#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

OPENAPI_FILE="src/jvmMain/resources/openapi.yaml"
PROMPT_FILE="scripts/prompts/update-api-docs.md"
VERIFIER_FILE="scripts/verify-api-docs.py"
RUNTIME_CONTRACT_FILE="docs/api/runtime-behavior.md"

command -v opencode >/dev/null 2>&1 || {
  echo "ERROR: opencode is not installed or not on PATH." >&2
  exit 1
}

command -v python3 >/dev/null 2>&1 || {
  echo "ERROR: python3 is required to verify the generated API documentation." >&2
  exit 1
}

for required_file in "$PROMPT_FILE" "$VERIFIER_FILE" "$RUNTIME_CONTRACT_FILE"; do
  [[ -f "$required_file" ]] || {
    echo "ERROR: required API documentation input is missing: $required_file" >&2
    exit 1
  }
done

# Fail early if the main source files used to document runtime behavior have moved
# or are missing. The agent is instructed to use these repository-relative paths.
REQUIRED_SOURCE_FILES=(
  "src/commonMain/kotlin/com/marzec/Api.kt"
  "src/commonMain/kotlin/com/marzec/extensions/LocalDateTimeExtensions.kt"
  "src/jvmMain/kotlin/com/marzec/extensions/LocalDateTimeExtensions.kt"
  "src/commonMain/kotlin/com/marzec/todo/model/model.kt"
  "src/jvmMain/kotlin/com/marzec/todo/database/TasksTable.kt"
  "src/jvmMain/kotlin/com/marzec/JvmMain.kt"
  "src/commonMain/kotlin/com/marzec/fiteo/model/domain/Training.kt"
  "src/commonMain/kotlin/com/marzec/fiteo/model/domain/TrainingExerciseWithProgress.kt"
  "src/commonMain/kotlin/com/marzec/cheatday/domain/Weight.kt"
  "src/commonMain/kotlin/com/marzec/fiteo/model/domain/FcmToken.kt"
)
for source_file in "${REQUIRED_SOURCE_FILES[@]}"; do
  [[ -r "$source_file" ]] || {
    echo "ERROR: required source file is missing or unreadable: $source_file" >&2
    exit 1
  }
done

# If OpenCode edits unrelated tracked or untracked files, fail rather than
# reporting a clean documentation sync. Baselines are captured after OpenAPI
# generation because that step is allowed to update openapi.yaml.
untracked_outside_docs_fingerprint() {
  while IFS= read -r -d '' file; do
    [[ "$file" == docs/api/* ]] && continue
    printf '%s ' "$file"
    sha256sum -- "$file" | awk '{print $1}'
  done < <(git ls-files --others --exclude-standard -z) | sort | sha256sum | awk '{print $1}'
}

echo "==> Generating OpenAPI snapshot from the Ktor routing tree..."
./gradlew generateOpenApiSnapshot

[[ -s "$OPENAPI_FILE" ]] || {
  echo "ERROR: generated OpenAPI file is missing or empty: $OPENAPI_FILE" >&2
  exit 1
}

OUTSIDE_DIFF_BEFORE="$(git diff --binary HEAD -- . ':(exclude)docs/api/**' | sha256sum | awk '{print $1}')"
UNTRACKED_OUTSIDE_BEFORE="$(untracked_outside_docs_fingerprint)"
RUNTIME_CONTRACT_BEFORE="$(sha256sum "$RUNTIME_CONTRACT_FILE" | awk '{print $1}')"

echo "==> Updating Markdown API documentation with OpenCode..."
opencode run --agent build "$(cat "$PROMPT_FILE")"

OUTSIDE_DIFF_AFTER="$(git diff --binary HEAD -- . ':(exclude)docs/api/**' | sha256sum | awk '{print $1}')"
UNTRACKED_OUTSIDE_AFTER="$(untracked_outside_docs_fingerprint)"
RUNTIME_CONTRACT_AFTER="$(sha256sum "$RUNTIME_CONTRACT_FILE" | awk '{print $1}')"

if [[ "$OUTSIDE_DIFF_BEFORE" != "$OUTSIDE_DIFF_AFTER" ]] || [[ "$UNTRACKED_OUTSIDE_BEFORE" != "$UNTRACKED_OUTSIDE_AFTER" ]]; then
  echo "ERROR: OpenCode changed files outside docs/api/. Inspect git diff; no changes were automatically reverted." >&2
  exit 1
fi

if [[ "$RUNTIME_CONTRACT_BEFORE" != "$RUNTIME_CONTRACT_AFTER" ]]; then
  echo "ERROR: OpenCode changed the maintained runtime contract: $RUNTIME_CONTRACT_FILE" >&2
  exit 1
fi

echo "==> Verifying runtime-critical API documentation invariants..."
python3 "$VERIFIER_FILE"

echo
echo "==> API documentation synchronization and verification finished."
echo "Review the result with:"
echo "  git diff -- docs/api"
echo "  git status --short -- docs/api"
