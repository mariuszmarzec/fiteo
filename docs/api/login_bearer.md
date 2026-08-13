# POST /fiteo/api/1/login-bearer

Logs in and receives a bearer token for authenticated requests.

## Description

Authenticates the user and returns a signed JWT in the `Authorization` header, which can then be used for bearer authentication.

## Authentication

Not required.

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

Returns the authenticated user record and creates an Authorization token for bearer-based requests.

### Response Body

```json
{
  "id": 1,
  "email": "user@example.com"
}
```

## Notes

The server creates a signed JWT and appends an `Authorization` header value. The existing documentation notes that the token value is masked in server logs.
