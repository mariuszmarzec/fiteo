# DELETE /fiteo/api/1/fcm-token/{id}

Deletes an FCM token by ID.

## Description

Removes the FCM token identified by the supplied path parameter for the authenticated user.

## Authentication

Required (session).

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | FCM token ID to delete. |

## Response

### Success

`200 OK`

The token was removed. The response body is an empty success value (`Unit`), so there is no useful JSON payload to show.

## Notes

This endpoint requires an authenticated session.