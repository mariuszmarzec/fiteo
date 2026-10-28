# PATCH /fiteo/api/1/equipment/{id}

Updates an existing piece of equipment by ID.

## Description

Partially updates the equipment identified by the supplied path parameter. Only the fields present in the request body are changed.

## Authentication

Not required.

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Equipment ID to update. |

## Request Body

`application/json` with arbitrary field names (`Map<String, JsonElement?>`). Send only the fields that should change, for example `id` or `name`.

### Example

```json
{
  "name": "Olympic Barbell"
}
```

## Response

### Success

`200 OK`

Returns the updated `EquipmentDto`.

### Response Body

```json
{
  "id": "1",
  "name": "Olympic Barbell"
}
```

## Notes

This endpoint is public and does not require a session or bearer token.