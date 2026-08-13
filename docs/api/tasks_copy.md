# GET /todo/api/1/tasks/{id}/copy

Copies an existing task.

## Description

Creates a copy of the task identified by the supplied task ID and returns the copied task.

## Authentication

Required.

Use the configured session or bearer authentication for the request.

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | Int | Yes | Task ID to copy. |

## Response

### Success

`200 OK`

Returns the copied `TaskDto`.

### Response Body

```json
{
  "id": 124,
  "ownerId": 42,
  "description": "Buy milk",
  "addedTime": "2026-08-13T11:00:00Z",
  "modifiedTime": "2026-08-13T11:00:00Z",
  "parentTaskId": null,
  "subTasks": [],
  "isToDo": true,
  "priority": 1,
  "scheduler": null,
  "expirationDate": null,
  "shares": []
}
```
