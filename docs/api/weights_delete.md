# DELETE /cheat/api/1/weights/{id}

Deletes a weight entry by ID.

## Description

Removes the weight entry identified by the supplied path parameter.

## Authentication

Required (session).

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Weight entry ID to delete. |

## Response

### Success

`200 OK`

Returns the deleted `WeightDto`.

### Response Body

```json
{
  "id": 1,
  "value": 75.5,
  "date": "2026-08-13T10:00:00Z"
}
```

## Notes

This endpoint requires an authenticated session.