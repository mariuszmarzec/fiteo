# DELETE /todo/api/1/tasks/{id}

Removes a task by ID.

## Description

Deletes a task for the authenticated user. The endpoint can also remove child tasks depending on the query parameter.

## Authentication

Required.

Use the configured session or bearer authentication for the request.

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | Int | Yes | Task ID to delete. |

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| removeWithSubtasks | Boolean | No | Whether to remove subtasks as well. Default is `false`. |

### Example

```http
DELETE /todo/api/1/tasks/123?removeWithSubtasks=true
```

## Response

### Success

`200 OK`

Returns the deleted `TaskDto`.

### Response Body

```json
{
  "id": 123,
  "ownerId": 42,
  "description": "Old task",
  "addedTime": "2026-08-10T08:00:00Z",
  "modifiedTime": "2026-08-10T08:00:00Z",
  "parentTaskId": null,
  "subTasks": [],
  "isToDo": false,
  "priority": 0,
  "scheduler": null,
  "expirationDate": null,
  "shares": []
}
```
