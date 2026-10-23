# POST /todo/api/1/tasks

Creates a new task for the authenticated user.

## Description

Creates a new task with the supplied metadata and scheduling information.

## Authentication

Required.

Use the configured session or bearer authentication for the request.

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Identifier query parameter accepted by the route. |

## Request Body

`CreateTaskDto`

| Field | Type | Required | Description |
|---|---|---|---|
| description | String | Yes | Task description. |
| parentTaskId | Int? | No | Parent task ID when creating a subtask. |
| priority | Int? | No | Task priority. |
| highestPriorityAsDefault | Boolean? | No | Whether to use the highest priority as the default behavior. |
| scheduler | SchedulerDto | No | Task scheduling configuration. |
| isToDo | Boolean? | No | Whether the task is a ToDo item. |
| expirationDate | String? | No | Expiration date as a local date-time string; see the date format note below. |
| shares | List<TaskShareDto>? | No | Task share configuration. |

### TaskShareDto

| Field | Type | Required | Description |
|---|---|---|---|
| userId | String | Yes | User ID the task is shared with. |
| permission | String | Yes | Permission level for the shared user. |

### SchedulerDto

| Field | Type | Required | Description |
|---|---|---|---|
| hour | Int | Yes | Hour for the schedule. |
| minute | Int | Yes | Minute for the schedule. |
| creationDate | String? | No | Creation date as a local date-time string; no timezone suffix. |
| startDate | String | Yes | Start date as a local date-time string; no timezone suffix. |
| lastDate | String? | No | Last date as a local date-time string; no timezone suffix. |
| daysOfWeek | List<Int> | Yes | Days of week for recurrence. |
| dayOfMonth | Int | Yes | Day of month for recurrence. |
| repeatCount | Int | Yes | Number of repeats. |
| repeatInEveryPeriod | Int | Yes | Repeat interval. |
| type | String | Yes | Case-sensitive scheduler type: `OneShot`, `Weekly`, or `Monthly`. |
| options | Map<String,String>? | No | Optional schedule options. |

### Scheduler type values

The `type` value is case-sensitive and must match the scheduler class name returned by the API:

- `OneShot` — a one-time schedule.
- `Weekly` — a weekly recurring schedule.
- `Monthly` — a monthly recurring schedule.

Do not use lowercase values such as `weekly`; the backend's scheduler mapping uses the exact names above.

### Date-time format

Task and scheduler date-time fields use the pattern `yyyy-MM-dd'T'HH:mm:ss`, for example `2026-08-13T09:00:00`. Send a local date-time without a timezone suffix. Do **not** append `Z` or a UTC offset such as `+02:00`; those forms are rejected by the current request parser.

This applies to `scheduler.creationDate`, `scheduler.startDate`, `scheduler.lastDate`, and `expirationDate`. Task date-time fields in responses use the same format.

### Example: weekly schedule

```json
{
  "description": "Buy milk",
  "priority": 1,
  "highestPriorityAsDefault": true,
  "scheduler": {
    "hour": 9,
    "minute": 0,
    "startDate": "2026-08-13T09:00:00",
    "daysOfWeek": [1, 3, 5],
    "dayOfMonth": 0,
    "repeatCount": 0,
    "repeatInEveryPeriod": 1,
    "type": "Weekly",
    "options": {
      "timezone": "Europe/Warsaw"
    }
  },
  "isToDo": true,
  "expirationDate": "2026-08-20T00:00:00"
}
```

### Example: one-time schedule

Use `type: "OneShot"` for a one-time task. The key value is case-sensitive.

```json
{
  "description": "Call the dentist",
  "scheduler": {
    "hour": 9,
    "minute": 0,
    "startDate": "2026-08-13T09:00:00",
    "daysOfWeek": [],
    "dayOfMonth": 0,
    "repeatCount": -1,
    "repeatInEveryPeriod": 1,
    "type": "OneShot"
  }
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
  "addedTime": "2026-08-13T10:00:00",
  "modifiedTime": "2026-08-13T10:00:00",
  "parentTaskId": null,
  "subTasks": [],
  "isToDo": true,
  "priority": 1,
  "scheduler": {
    "hour": 9,
    "minute": 0,
    "creationDate": "2026-08-13T10:00:00",
    "startDate": "2026-08-13T09:00:00",
    "lastDate": null,
    "daysOfWeek": [1, 3, 5],
    "dayOfMonth": 0,
    "repeatCount": 0,
    "repeatInEveryPeriod": 1,
    "type": "Weekly",
    "options": {
      "timezone": "Europe/Warsaw"
    }
  },
  "expirationDate": "2026-08-20T00:00:00",
  "shares": []
}
```
