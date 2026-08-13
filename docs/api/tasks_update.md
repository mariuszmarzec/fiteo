# PATCH /todo/api/1/tasks/{id}

Updates an existing task by ID.

## Description

Updates a task for the authenticated user. The request can change fields such as description, priority, scheduling, and expiration date.

## Authentication

Required.

Use the configured session or bearer authentication for the request.

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | Int | Yes | Task ID to update. |

## Request Body

`UpdateTaskDto`

| Field | Type | Required | Description |
|---|---|---|---|
| description | String? | No | New task description. |
| parentTaskId | NullableFieldDto<Int>? | No | Use this to set or clear the parent task ID. |
| priority | Int? | No | New priority value. |
| isToDo | Boolean? | No | Whether the task is marked as a ToDo item. |
| scheduler | NullableFieldDto<SchedulerDto>? | No | Use this to set or clear the scheduler configuration. |
| expirationDate | NullableFieldDto<String>? | No | Use this to set or clear the expiration date. |
| shares | List<UpdateTaskShareDto>? | No | Updated share configuration. |

### Example

```json
{
  "description": "Buy milk and bread",
  "priority": 2,
  "isToDo": false
}
```

## Response

### Success

`200 OK`

Returns the updated `TaskDto`.

### Response Body

```json
{
  "id": 1,
  "ownerId": 42,
  "description": "Buy milk and bread",
  "addedTime": "2026-08-13T10:00:00Z",
  "modifiedTime": "2026-08-13T10:30:00Z",
  "parentTaskId": null,
  "subTasks": [],
  "isToDo": false,
  "priority": 2,
  "scheduler": null,
  "expirationDate": null,
  "shares": []
}
```
