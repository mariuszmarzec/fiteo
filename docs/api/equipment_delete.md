# DELETE /fiteo/api/1/equipment/{id}

Deletes a piece of equipment by ID.

## Description

Removes the equipment identified by the supplied path parameter.

## Authentication

Not required.

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Equipment ID to delete. |

## Response

### Success

`200 OK`

Returns the deleted `EquipmentDto`.

### Response Body

```json
{
  "id": "1",
  "name": "Barbell"
}
```

## Notes

This endpoint is public and does not require a session or bearer token.