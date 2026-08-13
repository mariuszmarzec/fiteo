# POST /todo/api/1/tasks/leave-share

Leaves a shared task.

## Description

Removes the current user from a shared task. The source documentation describes this as leaving a shared task.

## Authentication

Required.

Use the configured session or bearer authentication for the request.

## Request Body

`LeaveShareDto`

| Field | Type | Required | Description |
|---|---|---|---|
| id | Int | Yes | ID of the shared task to leave. |

### Example

```json
{
  "id": 42
}
```

## Response

### Success

`200 OK`

Returns the updated task after leaving the share.

### Response Body

```json
{
  "id": 42,
  "ownerId": 9,
  "description": "Shared task",
  "addedTime": "2026-08-10T13:00:00Z",
  "modifiedTime": "2026-08-12T14:00:00Z",
  "parentTaskId": null,
  "subTasks": [],
  "isToDo": false,
  "priority": 3,
  "scheduler": null,
  "expirationDate": null,
  "shares": []
}
```
