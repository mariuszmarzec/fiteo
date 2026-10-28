# POST /fiteo/api/1/login

Logs in with email and password and establishes a session.

## Description

Authenticates the user and creates a session for subsequent authenticated requests.

## Authentication

Not required.

On successful login, the server sets the authenticated session in the response `Authorization` header. The response body contains the user record; the session token is not part of that JSON body.

For every subsequent protected request, copy the **entire value** of the successful login response's `Authorization` header into the request's `Authorization` header unchanged. Do not try to reconstruct, decode, or transform the token, and do not assume that the response body contains it.

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

Returns the authenticated user record. Read the session token from the HTTP response header, not from the JSON body.

### Response Body

```json
{
  "id": 1,
  "email": "user@example.com"
}
```

## Notes

Use this endpoint before calling protected ToDo endpoints when session-based authentication is configured. The exact `Authorization` header value returned by this endpoint is the value to reuse on subsequent authenticated requests. The separate `/fiteo/api/1/login-bearer` endpoint returns an explicit bearer token instead.
