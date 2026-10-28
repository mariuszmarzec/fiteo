# POST /fiteo/api/1/login-bearer

Logs in and receives a bearer token for authenticated requests.

## Description

Authenticates the user and returns a signed JWT in the `Authorization` response header for bearer authentication.

## Authentication

Not required.

The response header contains the complete value in the form `Bearer <token>`. Reuse that full value unchanged as the `Authorization` header on subsequent requests; do not strip the `Bearer ` prefix or add a second one.

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Identifier query parameter accepted by the route. |

## Request Body

`LoginRequestDto`

| Field | Type | Required | Description |
|---|---|---|---|
| email | String? | No | User email. |
| password | String? | No | User password. |

### Example

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123"
}
```

## Response

### Success

`200 OK`

Returns the authenticated user record and includes the bearer token in the `Authorization` response header. In the current implementation, the JWT expires after 60 seconds.

### Response Body

```json
{
  "id": 1,
  "email": "user@example.com"
}
```

## Notes

The token is signed by the server and the token value is not included in the JSON response body. For the regular session-based `/fiteo/api/1/login` flow, see [Login](login.md); its `Authorization` header value must likewise be reused exactly as returned.
