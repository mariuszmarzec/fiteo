# MCP tool contract: ToDo and CheatDay

This document defines the initial allowlist and argument contract for the remote MCP endpoint implemented by Fiteo. The initial release exposes only operations already supported by the existing ToDo and CheatDay controllers.

## Authentication and identity

- Endpoint: `/mcp`, using the official Kotlin MCP SDK's Streamable HTTP transport.
- Every MCP request requires `Authorization: Bearer <PAT>`.
- Authenticate using the existing `PersonalAccessTokenService.verify(token)`; do not duplicate hash, expiry, or revocation logic.
- Resolve the effective user exclusively from the verified PAT owner and build the existing user-scoped `HttpRequest` context for controller calls.
- No tool input may select the effective `userId`. In tools that create/update task shares, a target share's `userId` is ordinary domain data and must not be interpreted as caller identity.
- PAT create/list/delete endpoints are not MCP tools. The MCP server does not grant scopes or change the authentication model of existing REST routes.

## Tool naming and result format

Tool names are stable, lowercase, namespaced identifiers. Inputs are JSON objects with explicit schemas. Returned domain values are the existing serialized DTOs: `TaskDto`, `List<TaskDto>`, `WeightDto`, or `List<WeightDto>`, as shown below. Map existing controller/domain failures to protocol-appropriate tool errors without leaking stack traces, secrets, or database internals.

Do not introduce tools for generic HTTP calls, arbitrary paths/methods, SQL, or dynamic controller dispatch.

## Allowed tool inventory

| Tool name | Existing operation | Input fields | Effect |
|---|---|---|---|
| `todo_list_tasks` | `GET /todo/api/1/tasks` | none | Read task list for the PAT owner |
| `todo_create_task` | `POST /todo/api/1/tasks` | `task` object matching `CreateTaskDto` | Creates a task |
| `todo_update_task` | `PATCH /todo/api/1/tasks/{id}` | `id`, `task` object matching `UpdateTaskDto` | Updates an existing task |
| `todo_mark_tasks` | `POST /todo/api/1/tasks/mark-as-to-do` | `isToDo`, `taskIds` | Changes ToDo status for supplied tasks |
| `todo_copy_task` | `GET /todo/api/1/tasks/{id}/copy` | `id` | Creates a copy of a task |
| `todo_delete_task` | `DELETE /todo/api/1/tasks/{id}` | `id`, optional `removeWithSubtasks` | Deletes a task; destructive |
| `todo_leave_shared_task` | `POST /todo/api/1/tasks/leave-share` | `id` | Leaves the specified shared task |
| `cheatday_list_weights` | `GET /cheat/api/1/weights` | none | Reads weight history for the PAT owner |
| `cheatday_get_weight` | `GET /cheat/api/1/weights/{id}` | `id` | Reads one weight entry |
| `cheatday_create_weight` | `POST /cheat/api/1/weights` | `value`, `date` | Creates a weight entry |
| `cheatday_update_weight` | `PATCH /cheat/api/1/weights/{id}` | `id`, optional `value`, optional `date` | Updates a weight entry |
| `cheatday_delete_weight` | `DELETE /cheat/api/1/weights/{id}` | `id` | Deletes a weight entry; destructive |

The REST routes are listed here only to identify the behavior that each MCP tool wraps; MCP clients must call tools rather than pass REST paths to a generic proxy.

## Argument schemas and semantics

### Task listing — `todo_list_tasks`

Input: an empty JSON object `{}`.

Output: the existing `List<TaskDto>` result, scoped to the PAT owner.

### Task creation — `todo_create_task`

Input object:

```json
{
  "task": {
    "description": "Buy milk",
    "parentTaskId": null,
    "priority": 1,
    "highestPriorityAsDefault": true,
    "scheduler": null,
    "isToDo": true,
    "expirationDate": null,
    "shares": []
  }
}
```

The nested `task` follows `CreateTaskDto`; `description` is required, all other fields are optional/nullable as defined by the DTO. Scheduler date-time strings use `yyyy-MM-dd'T'HH:mm:ss` without timezone suffixes. Scheduler `type` values are case-sensitive: `OneShot`, `Weekly`, or `Monthly`. Scheduled tasks cannot have a parent; preserve the existing controller constraint. The result is a `TaskDto`.

References: [Create task REST contract](tasks_create.md).

### Task update — `todo_update_task`

Input object:

```json
{
  "id": 123,
  "task": {
    "description": "Buy milk and bread",
    "priority": 2,
    "isToDo": false
  }
}
```

`id` must be a positive integer. `task` follows `UpdateTaskDto`. Nullable-field wrappers (for example `parentTaskId`, `scheduler`, and `expirationDate`) must retain the REST contract's distinction between an omitted field and an explicitly cleared value. Preserve validation for self-parenting and scheduler/parent combinations. The result is a `TaskDto`.

Reference: [Update task REST contract](tasks_update.md).

### Mark tasks — `todo_mark_tasks`

Input object: `{"isToDo": true, "taskIds": [123, 124]}`.

- `isToDo`: required boolean.
- `taskIds`: required list of integer task IDs.
- Output: `List<TaskDto>`, as returned by the existing controller.

Reference: [Mark tasks as ToDo REST contract](tasks_mark_as_todo.md).

### Copy task — `todo_copy_task`

Input object: `{"id": 123}`. `id` is required. This operation creates a copy; it is not read-only. Output: `TaskDto`.

Reference: [Copy task REST contract](tasks_copy.md).

### Delete task — `todo_delete_task`

Input object: `{"id": 123, "removeWithSubtasks": false}`.

- `id`: required integer.
- `removeWithSubtasks`: optional boolean; defaults to `false`, matching the REST route.
- Output: the deleted `TaskDto`.
- Tool description must clearly explain the destructive behavior and child-task option.

Reference: [Delete task REST contract](tasks_delete.md).

### Leave a shared task — `todo_leave_shared_task`

Input object: `{"id": 123}`. `id` is required. Output: `TaskDto`. The task ID identifies the share/task to leave; caller identity always comes from the PAT owner.

Reference: [Leave shared task REST contract](tasks_leave_share.md).

### Weight history — `cheatday_list_weights`

Input: an empty JSON object `{}`.

Output: `List<WeightDto>`, each with `id` (integer), `value` (number), and `date` (string).

Reference: [Get weights REST contract](weights_get.md).

### Get one weight — `cheatday_get_weight`

Input object: `{"id": 123}`. `id` is required integer. Output: `WeightDto`.

Reference: [Get weight by ID REST contract](weights_get_by_id.md).

### Create weight — `cheatday_create_weight`

Input object: `{"value": 75.5, "date": "2026-08-13T10:00:00"}`.

- `value`: required number.
- `date`: required string using `yyyy-MM-dd'T'HH:mm:ss`, without `Z` or UTC offset.
- Output: `WeightDto`.

Reference: the create/put weights REST documentation and `PutWeightDto`.

### Update weight — `cheatday_update_weight`

Input object: `{"id": 123, "value": 76.0}`. `id` is required integer. Only supplied fields may be changed. Supported update fields are `value` and `date`; `date`, when supplied, uses `yyyy-MM-dd'T'HH:mm:ss` without timezone suffix. Do not turn this tool into a generic map/patch interface for arbitrary fields. Output: `WeightDto`.

Reference: [Update weight REST contract](weights_update.md).

### Delete weight — `cheatday_delete_weight`

Input object: `{"id": 123}`. `id` is required integer. This operation is destructive. Output: the removed `WeightDto`.

Reference: [Delete weight REST contract](weights_delete.md).

## Explicitly excluded capabilities

The MCP tool registry must not expose:

- Fiteo training, exercise, templates, equipment, categories, or pagination operations.
- Login, registration, logout, user administration, JWT/session management.
- PAT creation, listing, or deletion.
- Feature-toggle administration, scripts, SSE, force-load/test-only routes, or static resources.
- Generic HTTP/fetch, arbitrary URL/path/method dispatch, SQL, or dynamic method invocation.

No wildcard registration from REST routes is permitted. Adding a capability requires an explicit change to this contract, reviewed tool schema, focused tests, and parent issue update.

## Error and security expectations

- Missing/malformed/invalid/expired/revoked PAT must prevent MCP protocol operations, including initialize and tool listing.
- Data operations use the verified PAT owner. Supplying another user's ID must never change the effective caller.
- Validate all input fields before invoking the controller; preserve its domain validation and constraints.
- Never log or return the raw PAT, Authorization header, token hash, stack trace, or database internals.
- Mark task deletion, weight deletion, task copying, task creation, updates and status changes clearly in descriptions so callers can reason about side effects.
- Use the Kotlin SDK's supported Streamable HTTP transport and its DNS-rebinding/Origin protections appropriate to deployment. Do not hand-roll MCP/JSON-RPC parsing.

## Implementation sequencing

1. Finalize this inventory/contract in #66.
2. Add and verify the official Kotlin MCP SDK and endpoint skeleton in #67.
3. Require PAT authentication on every MCP request in #68.
4. Build the trusted PAT-owner request adapter in #69.
5. Implement tools independently in #70–#80, then hardening, security/E2E tests and usage documentation in #81–#84.
