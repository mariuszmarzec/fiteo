# POST /fiteo/api/1/login

Logs in with email and password and establishes a session.

## Description

Authenticates the user and creates a session for subsequent authenticated requests.

## Authentication

Not required.

On success, the server sets a session header or cookie for future requests. The existing documentation notes that the session may be sent in `Authorization` or `Authorization-Test` for test prefix flows.

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

Returns the authenticated user record.

### Response Body

```json
{
  "id": 1,
  "email": "user@example.com"
}
```

## Notes

Use this endpoint before calling protected ToDo endpoints when session-based authentication is configured.
