# GET /fiteo/api/1/equipment

Gets all equipment.

## Description

Returns the full list of equipment available in the system.

## Authentication

Not required.

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Identifier query parameter accepted by the route. |

## Response

### Success

`200 OK`

Returns a list of `EquipmentDto` objects.

### Response Body

```json
[
  {
    "id": "1",
    "name": "Barbell"
  },
  {
    "id": "2",
    "name": "Bench"
  }
]
```

## Notes

This endpoint is public and does not require a session or bearer token.