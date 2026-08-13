# Compile-time OpenAPI and Swagger UI

This document explains how to ensure that request and response schemas are generated for the API and what to do when Ktor cannot infer them.

## 1. Make sure request and response schemas are generated

Ktor can infer OpenAPI schemas from the types used in `call.receive<T>()` and `call.respond(...)`.

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

## 2. If request/response schemas are missing

First make sure **code inference** is enabled in the Ktor OpenAPI configuration:

```kotlin
ktor {
    openApi {
        enabled = true
        codeInferenceEnabled = true
        onlyCommented = false
    }
}
```

### Ensure your DTOs are explicitly typed and serializable

```kotlin
call.receive<CreateUserRequest>()
call.respond<UserResponse>(response)
```

Prefer using the typed `respond` overload rather than the generic `call.respond(response)`.

Make sure the DTO classes are annotated with `@Serializable`.

```kotlin
@Serializable
data class UserResponse(...)
```

### Add the OpenAPI routing dependency

```kotlin
implementation("io.ktor:ktor-server-routing-openapi:<ktor-version>")
```

## 3. When Ktor still cannot infer the schema

You can explicitly describe the request/response in the route using the `describe` DSL:

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

The exact API for explicit descriptions depends on the Ktor version, so prefer compiler inference first and add explicit OpenAPI metadata only where inference is insufficient.

## 4. Summary workflow

```
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

Following this approach avoids manually maintaining schemas and keeps the OpenAPI documentation synchronized with the Kotlin types.
