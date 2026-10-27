package com.marzec.fiteo

import com.marzec.common.createHttpRequest
import com.marzec.common.receiveHttpRequest
import com.marzec.common.respond
import com.marzec.di.Di
import com.marzec.fiteo.model.dto.CreatePersonalAccessTokenRequestDto
import com.marzec.fiteo.model.dto.ErrorDto
import com.marzec.fiteo.model.dto.PersonalAccessTokenCreationDto
import com.marzec.fiteo.model.dto.PersonalAccessTokenDto
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi

@OptIn(ExperimentalKtorApi::class)
fun Route.patApi(di: Di) {
    authenticate(di.authToken) {
        get(PatApiPath.TOKENS) {
            respond(di.personalAccessTokenApiController.getTokens(createHttpRequest(Unit)))
        }.describe {
            summary = "List personal access tokens"
            description = "Returns metadata for the authenticated user's personal access tokens without exposing token secrets or hashes."
            tag("Personal access tokens")
            responses {
                HttpStatusCode.OK {
                    description = "The authenticated user's personal access tokens."
                    ContentType.Application.Json {
                        schema = jsonSchema<List<PersonalAccessTokenDto>>()
                    }
                }
                HttpStatusCode.Unauthorized {
                    description = "A valid authenticated session is required."
                }
            }
        }

        post(PatApiPath.TOKENS) {
            respond(
                di.personalAccessTokenApiController.createToken(
                    receiveHttpRequest<CreatePersonalAccessTokenRequestDto>()
                )
            )
        }.describe {
            summary = "Create a personal access token"
            description = "Creates a token for the authenticated user. The raw token is returned only in this response."
            tag("Personal access tokens")
            requestBody {
                description = "Name and expiration policy for the new token."
                required = true
                ContentType.Application.Json {
                    schema = jsonSchema<CreatePersonalAccessTokenRequestDto>()
                }
            }
            responses {
                HttpStatusCode.OK {
                    description = "The personal access token was created."
                    ContentType.Application.Json {
                        schema = jsonSchema<PersonalAccessTokenCreationDto>()
                    }
                }
                HttpStatusCode.BadRequest {
                    description = "The name is blank or the expiration policy is unsupported."
                    ContentType.Application.Json {
                        schema = jsonSchema<ErrorDto>()
                    }
                }
                HttpStatusCode.Unauthorized {
                    description = "A valid authenticated session is required."
                }
            }
        }

        delete(PatApiPath.TOKEN_BY_ID) {
            respond(di.personalAccessTokenApiController.deleteToken(createHttpRequest(Unit)))
        }.describe {
            summary = "Delete a personal access token"
            description = "Deletes the token only when it belongs to the authenticated user. Other users' tokens are not affected."
            tag("Personal access tokens")
            responses {
                HttpStatusCode.OK {
                    description = "The token was deleted if it belongs to the authenticated user."
                }
                HttpStatusCode.Unauthorized {
                    description = "A valid authenticated session is required."
                }
            }
        }
    }
}
