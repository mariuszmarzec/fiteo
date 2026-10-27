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
        post(PatApiPath.TOKENS) {
            respond(
                di.personalAccessTokenApiController.createToken(
                    receiveHttpRequest<CreatePersonalAccessTokenRequestDto>()
                )
            )
        }
    }
}
