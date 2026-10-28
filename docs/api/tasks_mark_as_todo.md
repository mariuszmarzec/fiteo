# POST /todo/api/1/tasks/mark-as-to-do

Marks multiple tasks as ToDo or not ToDo.

## Description

Updates several tasks in one request by setting whether each task is marked as a ToDo item.

## Authentication

Required.

Use the configured session or bearer authentication for the request.

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Identifier query parameter accepted by the route. |

## Request Body

`MarkAsToDoDto`

| Field | Type | Required | Description |
|---|---|---|---|
| isToDo | Boolean | Yes | Whether the tasks should be marked as ToDo or not. |
| taskIds | List<Int> | Yes | List of task IDs to update. |

### Example

```json
{
  "isToDo": true,
  "taskIds": [1, 2, 3]
}
```

## Response

### Success

`200 OK`

Returns the updated task list.

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
  },
  {
    "id": 2,
    "ownerId": 42,
    "description": "Pay rent",
    "addedTime": "2026-08-12T09:00:00",
    "modifiedTime": "2026-08-13T09:15:00",
    "parentTaskId": null,
    "subTasks": [],
    "isToDo": true,
    "priority": 2,
    "scheduler": null,
    "expirationDate": null,
    "shares": []
  }
]
```

## Date-time format

API date-time values use `yyyy-MM-dd'T'HH:mm:ss` without a `Z` suffix or UTC offset, matching the backend's `Api.DATE_FORMAT`. The current parser does not accept timezone suffixes.

