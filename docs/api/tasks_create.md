# POST /todo/api/1/tasks

Creates a new task for the authenticated user.

## Description

Creates a new task with the supplied metadata and scheduling information.

## Authentication

Required.

Use the configured session or bearer authentication for the request.

## Request Body

`CreateTaskDto`

| Field | Type | Required | Description |
|---|---|---|---|
| description | String | Yes | Task description. |
| parentTaskId | Int? | No | Parent task ID when creating a subtask. |
| priority | Int? | No | Task priority. |
| highestPriorityAsDefault | Boolean? | No | Whether to use the highest priority as the default behavior. |
| scheduler | SchedulerDto? | No | Task scheduling configuration. |
| isToDo | Boolean? | No | Whether the task is a ToDo item. |
| expirationDate | String? | No | Expiration date in ISO date-time format. |
| shares | List<TaskShareDto>? | No | Task share configuration. |

### SchedulerDto

| Field | Type | Required | Description |
|---|---|---|---|
| hour | Int | Yes | Hour for the schedule. |
| minute | Int | Yes | Minute for the schedule. |
| creationDate | String? | No | Creation date in ISO date-time format. |
| startDate | String | Yes | Start date in ISO date-time format. |
| lastDate | String? | No | Last date in ISO date-time format. |
| daysOfWeek | List<Int> | Yes | Days of week for recurrence. |
| dayOfMonth | Int | Yes | Day of month for recurrence. |
| repeatCount | Int | Yes | Number of repeats. |
| repeatInEveryPeriod | Int | Yes | Repeat interval. |
| type | String | Yes | Schedule type. |
| options | Map<String,String>? | No | Optional schedule options. |

### Example

```json
{
  "description": "Buy milk",
  "priority": 1,
  "highestPriorityAsDefault": true,
  "scheduler": {
    "hour": 9,
    "minute": 0,
    "startDate": "2026-08-13T09:00:00Z",
    "daysOfWeek": [1, 3, 5],
    "dayOfMonth": 0,
    "repeatCount": 0,
    "repeatInEveryPeriod": 1,
    "type": "weekly",
    "options": {
      "timezone": "Europe/Warsaw"
    }
  },
  "isToDo": true,
  "expirationDate": "2026-08-20T00:00:00Z"
}
```

## Response

### Success

`200 OK`

Returns the created `TaskDto`.

### Response Body

```json
{
  "id": 1,
  "ownerId": 42,
  "description": "Buy milk",
  "addedTime": "2026-08-13T10:00:00Z",
  "modifiedTime": "2026-08-13T10:00:00Z",
  "parentTaskId": null,
  "subTasks": [],
  "isToDo": true,
  "priority": 1,
  "scheduler": {
    "hour": 9,
    "minute": 0,
    "creationDate": "2026-08-13T10:00:00Z",
    "startDate": "2026-08-13T09:00:00Z",
    "lastDate": null,
    "daysOfWeek": [1, 3, 5],
    "dayOfMonth": 0,
    "repeatCount": 0,
    "repeatInEveryPeriod": 1,
    "type": "weekly",
    "options": {
      "timezone": "Europe/Warsaw"
    }
  },
  "expirationDate": "2026-08-20T00:00:00Z",
  "shares": []
}
```
