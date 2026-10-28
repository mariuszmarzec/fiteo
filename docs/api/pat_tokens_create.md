# POST /pat/api/1/tokens

Creates a personal access token for the authenticated user.

## Description

Creates a token with a name and an expiration policy. The full token secret is returned only in this response. Store it securely when it is created; it cannot be retrieved again from the list endpoint.

## Authentication

Required. Use the application's existing authenticated session/authorization value in the `Authorization` request header. This endpoint does not use a PAT to authenticate token management.

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Optional identifier query parameter accepted by the route. |

## Request Body

`CreatePersonalAccessTokenRequestDto`

| Field | Type | Required | Description |
|---|---|---|---|
| name | String | Yes | A non-blank name used to identify the token in the token list. |
| expiresIn | String | Yes | Expiration policy: `THREE_MONTHS`, `SIX_MONTHS`, or `NEVER`. Values are case-sensitive. |

### Example

```json
{
  "name": "Local MCP",
  "expiresIn": "SIX_MONTHS"
}
```

## Response

### Success

`200 OK`

Returns `PersonalAccessTokenCreationDto`.

| Field | Type | Description |
|---|---|---|
| id | Int | Identifier of the created token. |
| name | String | Token name. |
| token | String | Full secret. This is the only response in which the full secret is returned. |
| prefix | String | Non-secret 8-character identifier derived from the token hash, not the beginning of the raw token. |
| createdAt | String | Creation date-time in the API's local date-time format. |
| expiresAt | String? | Expiration date-time, or `null` for `NEVER`. |

### Response Body Example

```json
{
  "id": 123,
  "name": "Local MCP",
  "token": "<full-token-returned-once>",
  "prefix": "a1b2c3d4",
  "createdAt": "2026-10-28T12:00:00",
  "expiresAt": "2027-04-28T12:00:00"
}
```

### Errors

- `400 Bad Request` — the name is blank or `expiresIn` is unsupported.
- `401 Unauthorized` — a valid authenticated session is required.

## Expiration policies

- `THREE_MONTHS` — expires three calendar months after creation.
- `SIX_MONTHS` — expires six calendar months after creation.
- `NEVER` — does not expire; `expiresAt` is `null`.

Date-time values use `yyyy-MM-dd'T'HH:mm:ss`, without a `Z` suffix or UTC offset.

## Security notes

The raw token is not stored in the database; only its SHA-256 hash and non-secret metadata are persisted. The full token is not included in list responses. Copy and store the creation response's `token` securely because it will not be shown again.
