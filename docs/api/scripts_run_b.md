# GET /api/run_B

Runs the list clearing script.

## Description

Executes the Python script that clears the shopping list. The script output is returned as plain text.

## Authentication

Required. HTTP Basic Authentication (`auth-basic`) using the credentials configured for the scripts panel (user `admin`, password `Kalka123!`).

## Response

### Success

`200 OK`

The script completed successfully. The response body is the plain-text output of the script.

### Response Body

```text
List cleared successfully
```

### Error

`500 Internal Server Error`

The script failed to execute. The response body is the plain-text error message.

## Notes

This endpoint is only available through the scripts panel, which is excluded from the generated OpenAPI document but is part of the running application.