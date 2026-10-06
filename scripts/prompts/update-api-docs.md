# Synchronize API documentation

Update the Markdown API documentation under `docs/api/` so it matches the current OpenAPI specification.

## Source of truth

Read:

- `src/jvmMain/resources/openapi.yaml` — authoritative API contract
- all existing files under `docs/api/`
- relevant Ktor routing/controller source files when additional human-readable context is useful

The OpenAPI document is authoritative for:

- paths
- HTTP methods
- path/query parameters
- request bodies
- response codes
- response schemas/content types
- server URL
- API-level metadata

Do not invent contract details that are not supported by OpenAPI or the source code.

## What to do

1. Compare the current OpenAPI paths and methods with the existing Markdown documentation.
2. Update existing Markdown files instead of replacing them wholesale when a file already documents the endpoint.
3. Preserve useful existing explanations, examples, authentication notes, and terminology when they are still correct.
4. Correct stale endpoint paths, HTTP methods, parameters, request/response schemas, status codes, and examples.
5. Create Markdown files for new API endpoints that do not have suitable existing documentation.
6. Remove or repoint documentation for endpoints that no longer exist only when it is clearly obsolete.
7. Update `docs/api/README.md` so its links and grouping match the resulting documentation.
8. Keep the current naming/style convention where practical. Prefer descriptive snake_case filenames such as `tasks_get.md`.
9. Keep endpoint documentation focused and readable. A typical endpoint page should contain:
   - title
   - HTTP method and path
   - purpose/description
   - authentication requirements when known
   - path/query parameters
   - request body
   - successful response
   - relevant error responses
   - a concise request example when the contract/source provides enough information
10. Use fenced code blocks for JSON/request examples.
11. Preserve the `openapi.md` page as documentation about the OpenAPI generation mechanism; update it when the implementation/configuration has materially changed.

## Important constraints

- Do NOT modify application source code.
- Do NOT modify Gradle/build configuration.
- Do NOT modify `src/jvmMain/resources/openapi.yaml`; the shell script has already generated it.
- Do NOT manually add endpoints to OpenAPI.
- Do NOT create documentation for routes excluded from the generated OpenAPI document.
- Do NOT silently delete useful existing documentation just because an endpoint is difficult to map.
- Do NOT commit changes. Leave the working tree ready for review.
- Keep unrelated files unchanged.

## Final verification

Before finishing:

- verify every OpenAPI path/method has corresponding documentation somewhere under `docs/api/`
- verify every endpoint listed in `docs/api/README.md` still exists in OpenAPI
- verify Markdown links in `docs/api/README.md` point to existing files
- review the diff and remove accidental/unrelated changes

At the end, briefly report which documentation files were created, updated, or removed.