package com.marzec.fiteo

import com.marzec.exceptions.HttpException
import com.marzec.exceptions.HttpStatus
import com.marzec.extensions.constraint
import com.marzec.extensions.formatDate
import com.marzec.extensions.getIntIdOrThrow
import com.marzec.extensions.serviceCall
import com.marzec.extensions.userIdOrThrow
import com.marzec.fiteo.model.domain.TokenExpiration
import com.marzec.fiteo.model.dto.CreatePersonalAccessTokenRequestDto
import com.marzec.fiteo.model.dto.PersonalAccessTokenCreationDto
import com.marzec.fiteo.model.dto.PersonalAccessTokenDto
import com.marzec.fiteo.model.http.HttpRequest
import com.marzec.fiteo.model.http.HttpResponse
import com.marzec.fiteo.services.PersonalAccessTokenService

class PersonalAccessTokenApiController(
    private val service: PersonalAccessTokenService,
    private val constraints: PersonalAccessTokenConstraints
) {

    fun createToken(
        request: HttpRequest<CreatePersonalAccessTokenRequestDto>
    ): HttpResponse<PersonalAccessTokenCreationDto> = request.serviceCall(
        constraints = listOf(
            constraints.nameIsNotBlank,
            constraints.expirationIsSupported
        )
    ) {
        val creation = service.create(
            userId = userIdOrThrow(),
            name = data.name.trim(),
            expiration = TokenExpiration.valueOf(data.expiresIn)
        )
        PersonalAccessTokenCreationDto(
            id = creation.pat.id,
            name = creation.pat.name,
            token = creation.token,
            prefix = creation.pat.tokenPrefix,
            createdAt = creation.pat.createdAt.formatDate(),
            expiresAt = creation.pat.expiresAt?.formatDate()
        )
    }

    fun getTokens(request: HttpRequest<Unit>): HttpResponse<List<PersonalAccessTokenDto>> = serviceCall {
        service.getTokensForUser(request.userIdOrThrow()).map { token ->
            PersonalAccessTokenDto(
                id = token.id,
                name = token.name,
                tokenPrefix = token.tokenPrefix,
                createdAt = token.createdAt.formatDate(),
                expiresAt = token.expiresAt?.formatDate(),
                lastUsedAt = token.lastUsedAt?.formatDate()
            )
        }
    }

    fun deleteToken(request: HttpRequest<Unit>): HttpResponse<Unit> = serviceCall {
        service.deleteToken(
            userId = request.userIdOrThrow(),
            tokenId = request.getIntIdOrThrow()
        )
    }
}

class PersonalAccessTokenConstraints {

    val nameIsNotBlank = constraint<CreatePersonalAccessTokenRequestDto>(
        breakingRule = { data.name.isBlank() },
        exception = {
            HttpException("Empty name", HttpStatus.BAD_REQUEST)
        }
    )

    val expirationIsSupported = constraint<CreatePersonalAccessTokenRequestDto>(
        breakingRule = {
            TokenExpiration.values().none { it.name == data.expiresIn }
        },
        exception = {
            HttpException("Unknown expiresIn", HttpStatus.BAD_REQUEST)
        }
    )
}
