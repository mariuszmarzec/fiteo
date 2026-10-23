# Synchronize API documentation

Update the Markdown API documentation under `docs/api/` so it matches the OpenAPI specification generated immediately before this OpenCode run.

## Mandatory reading and sources of truth

Read these sources before editing anything:

- `src/jvmMain/resources/openapi.yaml` — authoritative generated endpoint contract
- `docs/api/runtime-behavior.md` — mandatory maintained record of implementation behavior that OpenAPI cannot describe completely
- all existing endpoint pages under `docs/api/`
- relevant Kotlin routing, DTO, model, parser/formatter, and authentication files when needed

For runtime facts already covered in `runtime-behavior.md`, use it as a trusted contract note and confirm against source where useful. Known source locations include:

- `src/commonMain/kotlin/com/marzec/Api.kt` — shared date format and header/auth constants
- `src/commonMain/kotlin/com/marzec/extensions/LocalDateTimeExtensions.kt` and `src/jvmMain/kotlin/com/marzec/extensions/LocalDateTimeExtensions.kt` — date-time formatting declaration/implementation
- `src/commonMain/kotlin/com/marzec/todo/model/model.kt` — task/scheduler DTO conversion and scheduler type names
- `src/jvmMain/kotlin/com/marzec/todo/database/TasksTable.kt` — persisted scheduler mapping
- `src/jvmMain/kotlin/com/marzec/JvmMain.kt` — login routes and authentication headers
- `src/commonMain/kotlin/com/marzec/fiteo/model/domain/Training.kt` — training DTOs and date-time conversion
- `src/commonMain/kotlin/com/marzec/fiteo/model/domain/TrainingExerciseWithProgress.kt` — exercise-series DTO/date conversion
- `src/commonMain/kotlin/com/marzec/cheatday/domain/Weight.kt` — weight date conversion
- `src/commonMain/kotlin/com/marzec/fiteo/model/domain/FcmToken.kt` — FCM update-time conversion

OpenAPI is authoritative for paths, methods, parameters, request/response schemas, response codes, and content types. The maintained runtime-behavior document and current source code are authoritative for implementation details such as case-sensitive string values, date parsing/formatting, and authentication headers. These sources complement rather than replace one another.

## Important repository-navigation rules

- You are already running from the repository root. Use **repository-relative paths** such as `src/commonMain/kotlin/com/marzec/Api.kt`; do not reconstruct or hard-code an absolute workspace path like `/mnt/f/IdeaProjects/fiteo`.
- Do not run broad `find .`, `find /mnt/...`, or whole-repository scans that traverse `build/`, `build/js/node_modules/`, `.gradle/`, or other generated/dependency directories. Those scans waste context and commonly surface irrelevant files.
- To inspect API docs, use `find docs/api -maxdepth 1 -type f -name '*.md'`.
- To search tracked source code, prefer `git grep -n 'pattern' -- src/commonMain src/jvmMain`. If a source path is unexpected, verify it with `test -f path` or a narrowly scoped search under `src/` before reading it. Do not retry a failed read using a guessed or malformed absolute path.
- If a required source read genuinely fails, state which repository-relative path failed and do not claim that source-dependent verification succeeded.

## Runtime invariants that must survive every generation

1. **Scheduler types:** `SchedulerDto.type` values are exactly `OneShot`, `Weekly`, and `Monthly`, with the same capitalization. The task creation page must include a JSON example using `"type": "OneShot"`. Do not use lowercase `weekly` or infer values from the current OpenAPI example.
2. **Date-time strings across the whole API:** values backed by Kotlin `LocalDateTime` use `yyyy-MM-dd'T'HH:mm:ss` with no `Z` suffix, UTC offset, or fractional seconds. This applies beyond tasks, including training dates, exercise-series dates, weight dates, FCM update times, and other values converted through the shared `formatDate()`. Check every JSON request and response example in `docs/api/*.md`, including nested examples; do not limit this to task endpoints. If uncertain about a field, inspect its DTO/domain type and formatter.
3. **Authentication:** `POST /fiteo/api/1/login` returns the session authentication value in the `Authorization` response header, and callers must reuse that complete value unchanged in subsequent `Authorization` request headers. The JSON body contains the user, not the auth value. Keep this distinct from `POST /fiteo/api/1/login-bearer`, whose response header contains the full `Bearer <token>` value and whose token currently expires after 60 seconds.
4. **Do not generate the invariant source:** never rewrite, remove, or regenerate `docs/api/runtime-behavior.md` from OpenAPI. It is deliberately maintained separately to retain behavior that OpenAPI cannot reliably encode.

## What to do

1. Compare every OpenAPI path/method with the existing endpoint documentation.
2. Update existing Markdown files rather than replacing them wholesale when they already contain useful material.
3. Preserve correct explanations, examples, authentication notes, runtime caveats, and terminology.
4. Correct stale paths, methods, parameters, schemas, status codes, and examples.
5. Create endpoint Markdown files for new paths/methods that do not have a suitable page.
6. Remove or repoint documentation for obsolete endpoints only when that is clearly justified.
7. Keep `docs/api/README.md` links and groupings accurate, including its link to `runtime-behavior.md`.
8. Use descriptive snake_case filenames such as `tasks_get.md`.
9. Keep pages focused: purpose, authentication, parameters, request/response schemas, successful/error responses, and concise examples when useful.
10. Use fenced code blocks for request and response examples.
11. Preserve `openapi.md` as documentation about OpenAPI generation; update it only when that mechanism has materially changed.

## Constraints

- Do NOT modify application source code.
- Do NOT modify Gradle/build configuration or any scripts, validators, or prompt files.
- Do NOT modify `src/jvmMain/resources/openapi.yaml`; it has already been generated by Gradle.
- Do NOT manually add endpoints to OpenAPI.
- Do NOT create endpoint docs for routes excluded from the generated OpenAPI document.
- Do NOT alter `docs/api/runtime-behavior.md`; its owner maintains it separately.
- Do NOT silently delete useful documentation just because a route or schema is difficult to map.
- Do NOT commit changes. Leave the working tree ready for review.
- Keep unrelated files unchanged.

## Final verification

Before finishing:

- verify every OpenAPI path/method has corresponding endpoint documentation under `docs/api/`
- verify every endpoint linked in `docs/api/README.md` still exists and every link resolves
- inspect runtime examples against `docs/api/runtime-behavior.md` and relevant source code
- confirm all JSON date-time examples across the whole `docs/api/` directory match the actual local date-time format
- check that login docs describe the actual response header behavior
- review the diff and remove accidental/unrelated changes

The `scripts/update-api-docs.sh` caller runs a deterministic validator after this agent finishes. If the validator fails, do not report success; summarize the failure without weakening or removing the guard.

At the end, briefly report which documentation files were created, updated, or removed.
