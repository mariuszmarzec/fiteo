# GET /fiteo/api/1/equipment/{id}

Gets a single piece of equipment by ID.

## Description

Returns the equipment identified by the supplied path parameter.

## Authentication

Not required.

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Equipment ID. |

## Response

### Success

`200 OK`

Returns the requested `EquipmentDto`.

### Response Body

```json
{
  "id": "1",
  "name": "Barbell"
}
```

## Notes

This endpoint is public and does not require a session or bearer token.