# GET /pat/api/1/tokens

Lists the authenticated user's personal access tokens.

## Description

Returns token metadata for the current user. The list never includes the raw token secret or its hash, and it cannot be used to recover a token secret.

## Authentication

Required. Use the application's existing authenticated session/authorization value in the `Authorization` request header.

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Optional identifier query parameter accepted by the route. |

## Response

### Success

`200 OK`

Returns a list of `PersonalAccessTokenDto` objects.

| Field | Type | Description |
|---|---|---|
| id | Int | Token identifier. |
| name | String | Name chosen when creating the token. |
| tokenPrefix | String | Non-secret 8-character identifier derived from the token hash, not the beginning of the raw token. |
| createdAt | String | Creation date-time. |
| expiresAt | String? | Expiration date-time, or `null` for `NEVER`. |
| lastUsedAt | String? | Last successful verification time, or `null` if the token has not been used successfully. |

### Response Body Example

```json
[
  {
    "id": 123,
    "name": "Local MCP",
    "tokenPrefix": "a1b2c3d4",
    "createdAt": "2026-10-28T12:00:00",
    "expiresAt": "2027-04-28T12:00:00",
    "lastUsedAt": null
  }
]
```

### Errors

- `401 Unauthorized` — a valid authenticated session is required.

## Notes

The result is always scoped to the authenticated user's ID. It does not expose tokens owned by other users. Neither the raw token secret nor the SHA-256 hash is returned. Date-time values use `yyyy-MM-dd'T'HH:mm:ss`, without a `Z` suffix or UTC offset.
