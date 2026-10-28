# GET /sse

Server-Sent Events (SSE) stream.

## Description

Opens a long-lived Server-Sent Events connection for the authenticated user. The server sends a `keep-alive` comment every 15 seconds and an `UPDATE` data event whenever a relevant update event is published for the user.

## Authentication

Required (session).

## Response

### Success

`200 OK`

The connection is kept open as an event stream (`text/event-stream`). The response is not a single JSON payload but a continuous stream of events.

### Response Body

```text
: keep-alive

data: UPDATE
```

## Notes

This endpoint requires an authenticated session. It is excluded from the static OpenAPI schema generation in the sense that it is a streaming endpoint, but it is kept in the generated OpenAPI document.