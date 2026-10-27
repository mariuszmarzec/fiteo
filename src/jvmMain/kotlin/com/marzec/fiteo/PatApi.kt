package com.marzec.fiteo

import com.marzec.database.UserPrincipal
import com.marzec.di.Di
import com.marzec.fiteo.model.domain.TokenExpiration
import com.marzec.fiteo.model.dto.CreatePersonalAccessTokenRequestDto
import com.marzec.fiteo.model.dto.ErrorDto
import com.marzec.fiteo.model.dto.PersonalAccessTokenCreationDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.patApi(di: Di) {
    authenticate(di.authToken) {
        post(PatApiPath.TOKENS) {
            val userId = call.principal<UserPrincipal>()?.id
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorDto("Unauthorized"))
                return@post
            }

            val request = call.receive<CreatePersonalAccessTokenRequestDto>()
            val name = request.name.trim()
            if (name.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, ErrorDto("Empty name"))
                return@post
            }

            val expiration = when (request.expiresIn) {
                TokenExpiration.THREE_MONTHS.name -> TokenExpiration.THREE_MONTHS
                TokenExpiration.SIX_MONTHS.name -> TokenExpiration.SIX_MONTHS
                TokenExpiration.NEVER.name -> TokenExpiration.NEVER
                else -> null
            }
            if (expiration == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorDto("Unknown expiresIn"))
                return@post
            }

            val creation = di.personalAccessTokenService.create(userId, name, expiration)
            call.respond(
                PersonalAccessTokenCreationDto(
                    id = creation.pat.id,
                    name = creation.pat.name,
                    token = creation.token,
                    prefix = creation.pat.tokenPrefix,
                    createdAt = creation.pat.createdAt,
                    expiresAt = creation.pat.expiresAt
                )
            )
        }
    }
}
