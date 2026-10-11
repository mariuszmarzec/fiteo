# POST /fiteo/api/1/fcm-token

Registers an FCM token for the authenticated user.

## Description

Registers a Firebase Cloud Messaging (FCM) token for the current user so the server can send push notifications.

## Authentication

Required (session).

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Identifier query parameter accepted by the route. |

## Request Body

`CreateFcmTokenDto`

| Field | Type | Required | Description |
|---|---|---|---|
| fcmToken | String | Yes | FCM registration token. |
| platform | String? | No | Platform the token belongs to (e.g. `android`, `ios`). |

### Example

```json
{
  "fcmToken": "abc123def456",
  "platform": "android"
}
```

## Response

### Success

`200 OK`

Returns the registered `FcmTokenDto`.

### Response Body

```json
{
  "id": 1,
  "userId": 42,
  "fcmToken": "abc123def456",
  "platform": "android",
  "updatedAt": "2026-08-13T10:00:00Z"
}
```

## Notes

This endpoint requires an authenticated session.