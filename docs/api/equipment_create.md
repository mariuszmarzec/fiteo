# POST /fiteo/api/1/equipment

Creates new equipment.

## Description

Creates a new piece of equipment with the supplied identifier and name.

## Authentication

Not required.

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Identifier query parameter accepted by the route. |

## Request Body

`EquipmentDto`

| Field | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Equipment identifier. |
| name | String | Yes | Equipment name. |

### Example

```json
{
  "id": "1",
  "name": "Barbell"
}
```

## Response

### Success

`200 OK`

Returns the created `EquipmentDto`.

### Response Body

```json
{
  "id": "1",
  "name": "Barbell"
}
```

## Notes

This endpoint is public and does not require a session or bearer token.