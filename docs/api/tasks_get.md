# GET /todo/api/1/tasks

Gets all tasks for the authenticated user.

## Description

Returns the full list of tasks owned by the authenticated user.

## Authentication

Required.

Use the configured session or bearer authentication for the request.

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Identifier query parameter accepted by the route. |

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
    "addedTime": "2026-08-13T10:00:00",
    "modifiedTime": "2026-08-13T10:05:00",
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

Task and scheduler date-time fields are returned in the local date-time format `yyyy-MM-dd'T'HH:mm:ss`, without a `Z` suffix or UTC offset. When a task has a scheduler, its `scheduler.type` value is case-sensitive: `OneShot`, `Weekly`, or `Monthly`. See [Create task](tasks_create.md) for examples and request-field details.
