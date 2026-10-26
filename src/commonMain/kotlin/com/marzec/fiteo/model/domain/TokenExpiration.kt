package com.marzec.fiteo.model.domain

/**
 * How long a Personal Access Token should be valid for.
 *
 * Expiration is calculated using UTC calendar-month arithmetic (see
 * [PersonalAccessTokenService.calculateExpiration]), never as a fixed number
 * of days. [NEVER] keeps the token valid until it is deleted by the owner.
 */
enum class TokenExpiration {
    THREE_MONTHS,
    SIX_MONTHS,
    NEVER
}