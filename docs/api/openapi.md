# Compile-time OpenAPI and Swagger UI

This document explains how to ensure that request and response schemas are generated for the API and what to do when Ktor cannot infer them.

## Make sure request and response schemas are generated

Ktor can infer OpenAPI schemas from the types used in `call.receive<T>()` and `call.respond(...)`.

For example:

```kotlin
@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String
)

@Serializable
data class UserResponse(
    val id: Long,
    val name: String,
    val email: String
)
```

Then use the types directly in your routes:

```kotlin
post("/users") {
    val request = call.receive<CreateUserRequest>()

    val user = createUser(request)

    call.respond<UserResponse>(user)
}
```

This gives OpenAPI enough information to generate:

```yaml
components:
  schemas:
    CreateUserRequest:
      type: object
      properties:
        name:
          type: string
        email:
          type: string
      required:
        - name
        - email

    UserResponse:
      type: object
      properties:
        id:
          type: integer
          format: int64
        name:
          type: string
        email:
          type: string
```

## If request/response schemas are missing

First make sure:

1. `codeInferenceEnabled = true` is enabled:

```kotlin
ktor {
    openApi {
        enabled = true
        codeInferenceEnabled = true
        onlyCommented = false
    }
}
```

2. Your DTOs are explicitly typed:

```kotlin
call.receive<CreateUserRequest>()
```

and preferably:

```kotlin
call.respond<UserResponse>(response)
```

instead of relying on an untyped value:

```kotlin
call.respond(response)
```

3. DTOs are serializable:

```kotlin
@Serializable
data class UserResponse(...)
```

4. `ktor-server-routing-openapi` is present:

```kotlin
implementation("io.ktor:ktor-server-routing-openapi:<ktor-version>")
```

## If Ktor still cannot infer the schema

You can explicitly describe the request/response in the route:

```kotlin
post("/users") {
    // ...
}.describe {
    request {
        body<CreateUserRequest>()
    }
    response {
        body<UserResponse>()
    }
}
```

The exact API for explicit descriptions depends on the Ktor version, so **prefer compiler inference first** and add explicit OpenAPI metadata only where inference is insufficient.

In practice, the recommended approach is:

```text
DTO
 ↓
@Serializable
 ↓
call.receive<CreateUserRequest>()
call.respond<UserResponse>()
 ↓
Ktor compiler inference
 ↓
OpenAPI schema
```

This avoids manually maintaining schemas and keeps the OpenAPI documentation synchronized with the Kotlin types.

## Synchronizing Markdown API documentation

The generated OpenAPI document is the source of truth for the human-readable API documentation.

Run from the repository root:

```bash
./scripts/update-api-docs.sh
```

The script:

1. runs `./gradlew generateOpenApiSnapshot` to regenerate `src/jvmMain/resources/openapi.yaml` from the real application routing tree;
2. runs local OpenCode in non-interactive mode;
3. asks OpenCode to compare the generated contract with the existing `docs/api/*.md` files;
4. updates, creates, or removes Markdown documentation as needed and keeps `docs/api/README.md` links in sync.

The script does not commit changes. Review the result with:

```bash
git diff -- docs/api
```

OpenCode uses the local model/provider configuration. The synchronization prompt is stored in `scripts/prompts/update-api-docs.md`.

## OpenAPI and Swagger UI endpoints

At runtime the application exposes two UI endpoints that are generated from the
Ktor routing tree:

| Path | Purpose |
|---|---|
| `/openapi` | Serves the generated OpenAPI document (`openapi.yaml`). |
| `/swagger` | Serves the Swagger UI that renders the OpenAPI document. |

Both endpoints filter the routing tree before generating the document. The
following routes are excluded:

- root static resources (`/`, `/fiteo.js`)
- the scripts panel (`/scripts`)
- any test routes (`/test/...`)

The `/sse` endpoint is kept in the generated document.

The server URL and API metadata are configured at build time via
`BuildKonfig.OPENAPI_SERVER_URL`:

```kotlin
openAPI(path = "openapi") {
    info = OpenApiInfo("Fiteo API", "1.0")
    BuildKonfig.OPENAPI_SERVER_URL.takeIf { it.isNotBlank() }?.let { url ->
        servers { server(url) }
    }
    source = OpenApiDocSource.Routing(ContentType.Application.Yaml) {
        routingRoot.descendants().filterNot { route -> route.manulExcludedFromOpenApi() }
    }
}
```
