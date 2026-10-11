# PATCH /cheat/api/1/weights/{id}

Updates an existing weight entry by ID.

## Description

Partially updates the weight entry identified by the supplied path parameter. Only the fields present in the request body are changed.

## Authentication

Required (session).

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Weight entry ID to update. |

## Request Body

`application/json` with arbitrary field names (`Map<String, JsonElement?>`). Send only the fields that should change, for example `value` or `date`.

### Example

```json
{
  "value": 76.0
}
```

## Response

### Success

`200 OK`

Returns the updated `WeightDto`.

### Response Body

```json
{
  "id": 1,
  "value": 76.0,
  "date": "2026-08-13T10:00:00Z"
}
```

## Notes

This endpoint requires an authenticated session.