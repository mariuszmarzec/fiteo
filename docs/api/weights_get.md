# GET /cheat/api/1/weights

Gets all weight entries for the authenticated user.

## Description

Returns the full list of weight entries recorded by the authenticated user.

## Authentication

Required (session).

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Identifier query parameter accepted by the route. |

## Response

### Success

`200 OK`

Returns a list of `WeightDto` objects.

### Response Body

```json
[
  {
    "id": 1,
    "value": 75.5,
    "date": "2026-08-13T10:00:00"
  },
  {
    "id": 2,
    "value": 75.0,
    "date": "2026-08-06T09:00:00"
  }
]
```

## Date-time format

API date-time values use `yyyy-MM-dd'T'HH:mm:ss` without a `Z` suffix or UTC offset, matching the backend's `Api.DATE_FORMAT`. The current parser does not accept timezone suffixes.

## Notes

This endpoint requires an authenticated session.