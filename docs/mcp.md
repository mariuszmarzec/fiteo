# Fiteo remote MCP: ToDo and CheatDay

The backend exposes a remote Model Context Protocol (MCP) Streamable HTTP endpoint at `/mcp`. Every request must use a Personal Access Token (PAT) belonging to the user whose data is being accessed.

## Client configuration

Use the server's HTTPS URL in production and supply a PAT in the `Authorization: Bearer <PAT>` header. Store the token in your MCP client's secret store or an environment variable; do not commit it to configuration files, paste it into logs, or share it.

Example configuration (adapt the URL and environment-variable syntax to your MCP client):

```json
{
  "mcpServers": {
    "fiteo": {
      "url": "https://YOUR_FITEO_HOST/mcp",
      "headers": {
        "Authorization": "Bearer ${FITEO_PAT}"
      }
    }
  }
}
```

Check that your MCP client supports environment-variable expansion in HTTP headers. If it does not, use the client's supported secret injection mechanism rather than putting a literal PAT into this file.

## Create and revoke a PAT

Create a PAT through Fiteo's existing Personal Access Token settings/API while authenticated as the intended user. Select an expiration appropriate to your use; revoke/delete the token from the same PAT-management interface when it is no longer needed. The server validates each request with the existing PAT service, so expired and deleted tokens stop working.

Treat a PAT like a password. Never put the token in issue reports, source code, shell history, URLs, screenshots, or support logs.

## Supported tools

The server registers only the explicitly named ToDo and CheatDay tools below. Inputs are JSON objects; unexpected fields and invalid types are rejected.

| Tool | Inputs | Effect |
|---|---|---|
| `todo_list_tasks` | No arguments | Lists tasks visible to the authenticated user. |
| `todo_create_task` | `task`: existing `CreateTaskDto` object | Creates a task using existing ToDo rules. Shares requested in the task are granted according to existing permissions. |
| `todo_update_task` | `id`: positive integer; `task`: existing partial `UpdateTaskDto` object | Updates an accessible task. Omitted fields remain unchanged; nullable fields use the existing wrapper semantics to clear values. |
| `todo_mark_tasks` | `isToDo`: boolean; `taskIds`: non-empty array of positive integers | Changes the ToDo state of the supplied tasks. |
| `todo_copy_task` | `id`: positive integer | Creates a copy of an accessible task. |
| `todo_delete_task` | `id`: positive integer; optional `removeWithSubtasks`: boolean (default false) | Permanently deletes an accessible task; set the flag to also remove subtasks. |
| `todo_leave_shared_task` | `id`: positive integer | Removes the authenticated user's share of a shared task and may remove their access to it. |
| `cheatday_list_weights` | No arguments | Lists weight entries visible to the authenticated user. |
| `cheatday_get_weight` | `id`: positive integer | Reads a single accessible weight entry. |
| `cheatday_create_weight` | `value`: finite number; `date`: local date-time string | Creates a weight entry. The date is a local date-time without a timezone suffix. |
| `cheatday_update_weight` | `id`: positive integer; optional `value`: finite number; optional `date`: local date-time string | Partially updates a weight entry; omitted fields remain unchanged. |
| `cheatday_delete_weight` | `id`: positive integer | Permanently deletes an accessible weight entry. |

The tools reuse the existing controller/domain behavior and return JSON-serialized DTOs or lists of DTOs. Controller/domain failures are reported as MCP tool errors. Internal failures return a generic error rather than implementation details.

## Identity and access control

The effective user is derived exclusively from the owner of the verified PAT. Callers cannot select another user by passing a `userId`. Existing per-user permissions and access checks continue to apply; guessing another user's task or weight ID does not grant access.

Each MCP request, including initialization and tool discovery, requires `Authorization: Bearer <PAT>`. Missing, malformed, invalid, expired, and deleted/revoked tokens are rejected. Keep the endpoint behind HTTPS. Host/Origin allowlists are not enforced by the MCP application; network exposure and transport-level protections belong to the deployment/server layer.

## Explicit exclusions

This MCP server is not a generic REST proxy. It does not expose arbitrary HTTP requests, arbitrary paths or methods, SQL access, Fiteo training/exercise endpoints, login/register/session routes, PAT creation/list/deletion tools, feature-toggle administration, scripts, SSE, test-only routes, or other backend functionality. Existing non-MCP authentication behavior is unchanged.
