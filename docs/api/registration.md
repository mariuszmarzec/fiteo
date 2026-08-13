# POST /fiteo/api/1/registration

Registers a new user.

## Description

Creates a new user account using an email and password.

## Authentication

Not required.

## Request Body

`RegisterRequestDto`

| Field | Type | Required | Description |
|---|---|---|---|
| email | String | Yes | User email address. |
| password | String | Yes | User password. |
| repeatedPassword | String | Yes | Password confirmation. |

### Example

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123",
  "repeatedPassword": "StrongPassword123"
}
```

## Response

### Success

`200 OK`

Returns the created user record.

### Response Body

```json
{
  "id": 1,
  "email": "user@example.com"
}
```

## Notes

This endpoint is public and does not require a session or bearer token.
