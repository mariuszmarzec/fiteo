# GET /cheat/api/1/weights/{id}

Gets a single weight entry by ID.

## Description

Returns the weight entry identified by the supplied path parameter.

## Authentication

Required (session).

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Weight entry ID. |

## Response

### Success

`200 OK`

Returns the requested `WeightDto`.

### Response Body

```json
{
  "id": 1,
  "value": 75.5,
  "date": "2026-08-13T10:00:00"
}
```

## Date-time format

API date-time values use `yyyy-MM-dd'T'HH:mm:ss` without a `Z` suffix or UTC offset, matching the backend's `Api.DATE_FORMAT`. The current parser does not accept timezone suffixes.

## Notes

This endpoint requires an authenticated session.