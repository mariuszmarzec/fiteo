# GET /fiteo/api/1/users

Gets all users.

## Description

Returns the list of users available in the current authenticated context. The source documentation describes this as an admin/test flow that requires appropriate auth.

## Authentication

Required (session).

## Response

### Success

`200 OK`

Returns a list of `UserDto` objects.

### Response Body

```json
[
  {
    "id": 1,
    "email": "user@example.com"
  },
  {
    "id": 2,
    "email": "another-user@example.com"
  }
]
```

## Notes

This endpoint is intended for admin/test flows and requires the appropriate user session or permissions.
