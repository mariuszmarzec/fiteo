# GET /todo/api/1/tasks

Gets all tasks for the authenticated user.

## Description

Returns the full list of tasks owned by the authenticated user.

## Authentication

Required.

Use the configured session or bearer authentication for the request.

## Response

### Success

`200 OK`

The response contains a list of `TaskDto` entries.

### Response Body

```json
[
  {
    "id": 1,
    "ownerId": 42,
    "description": "Buy milk",
    "addedTime": "2026-08-13T10:00:00Z",
    "modifiedTime": "2026-08-13T10:05:00Z",
    "parentTaskId": null,
    "subTasks": [],
    "isToDo": true,
    "priority": 1,
    "scheduler": null,
    "expirationDate": null,
    "shares": []
  }
]
```

## Notes

All ToDo endpoints in this documentation require authentication.
