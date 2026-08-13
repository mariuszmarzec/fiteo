# GET /fiteo/api/1/user

Gets the current authenticated user.

## Description

Returns the user profile for the currently authenticated session.

## Authentication

Required (session).

The request must include the active session cookie or session header used by the application.

## Response

### Success

`200 OK`

Returns the authenticated user.

### Response Body

```json
{
  "id": 1,
  "email": "user@example.com"
}
```
