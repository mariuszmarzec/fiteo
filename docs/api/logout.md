# GET /fiteo/api/1/logout

Logs out the current user and clears the session.

## Description

Ends the current authenticated session and clears session state. The source documentation also notes that this deletes FCM tokens for the user on the server side.

## Authentication

Required (session).

## Response

### Success

`200 OK`

The request completes successfully and the session is cleared.

### Response Body

The operation returns an empty success value (`Unit`), so there is no useful JSON payload to show.
