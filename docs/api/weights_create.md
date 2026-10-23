# POST /cheat/api/1/weights

Records a new weight entry for the authenticated user.

## Description

Creates a new weight entry with the supplied value and date.

## Authentication

Required (session).

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Identifier query parameter accepted by the route. |

## Request Body

`PutWeightDto`

| Field | Type | Required | Description |
|---|---|---|---|
| value | Float | Yes | Weight value. |
| date | String | Yes | Date in ISO date-time format. |

### Example

```json
{
  "value": 75.5,
  "date": "2026-08-13T10:00:00"
}
```

## Response

### Success

`200 OK`

Returns the created `WeightDto`.

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