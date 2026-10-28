# DELETE /pat/api/1/tokens/{id}

Deletes (revokes) a personal access token owned by the authenticated user.

## Description

Removes the token identified by the path parameter. The deletion operation is scoped to the authenticated user's ID; a token belonging to another user is not deleted.

## Authentication

Required. Use the application's existing authenticated session/authorization value in the `Authorization` request header.

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | ID of the personal access token to revoke. |

## Response

### Success

`200 OK`

Returns an empty success value (`Unit`); there is no useful JSON response body.

### Errors

- `401 Unauthorized` — a valid authenticated session is required.

## Notes

After deletion, the token no longer passes PAT verification. A request targeting a token owned by another user leaves that token untouched. The endpoint does not support editing tokens; create a replacement token if needed.
