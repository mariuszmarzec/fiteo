# GET /api/run_A

Runs the shopping list generation script.

## Description

Executes the Python script that generates a shopping list, optionally filtered by a date range and limited to ToDo items. The script output is returned as plain text.

## Authentication

Required. HTTP Basic Authentication (`auth-basic`) using the credentials configured for the scripts panel (user `admin`, password `Kalka123!`).

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| dateFrom | String | No | Start date in `YYYY-MM-DD` format. |
| dateTo | String | No | End date in `YYYY-MM-DD` format. |
| onlyTodo | String | No | When truthy, only ToDo items are included. |

### Example

```http
GET /api/run_A?dateFrom=2026-08-01&dateTo=2026-08-31&onlyTodo=true
```

## Response

### Success

`200 OK`

The script completed successfully. The response body is the plain-text output of the script.

### Response Body

```text
Shopping list generated successfully
- Milk
- Bread
- Eggs
```

### Error

`500 Internal Server Error`

The script failed to execute. The response body is the plain-text error message.

## Notes

This endpoint is only available through the scripts panel, which is excluded from the generated OpenAPI document but is part of the running application.