// Enable OpenAPI code inference
ktor {
    openApi {
        enabled = true
        codeInferenceEnabled = true
        onlyCommented = false
    }
}

// Define serializable DTOs
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

// Usage in route
post("/users") {
    val request = call.receive<CreateUserRequest>()

    val user = createUser(request)

    call.respond<UserResponse>(user)
}