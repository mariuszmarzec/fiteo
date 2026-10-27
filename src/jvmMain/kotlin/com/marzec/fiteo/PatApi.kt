package com.marzec.fiteo

import com.marzec.common.receiveHttpRequest
import com.marzec.common.respond
import com.marzec.di.Di
import com.marzec.fiteo.model.dto.CreatePersonalAccessTokenRequestDto
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.patApi(di: Di) {
    authenticate(di.authToken) {
        /**
         * Create a personal access token for the authenticated user.
         *
         * Tag: Personal access tokens
         * Body: application/json [com.marzec.fiteo.model.dto.CreatePersonalAccessTokenRequestDto] Token name and expiration policy.
         * Response: 200 application/json [com.marzec.fiteo.model.dto.PersonalAccessTokenCreationDto] Created token and metadata. The raw token is returned only in this response.
         * Response: 400 application/json [com.marzec.fiteo.model.dto.ErrorDto] The name is blank or the expiration policy is unsupported.
         */
        post(PatApiPath.TOKENS) {
            respond(
                di.personalAccessTokenApiController.createToken(
                    receiveHttpRequest<CreatePersonalAccessTokenRequestDto>()
                )
            )
        }
    }
}
