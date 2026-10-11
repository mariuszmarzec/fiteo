# PATCH /fiteo/api/1/categories/{id}

Updates an existing category by ID.

## Description

Partially updates the category identified by the supplied path parameter. Only the fields present in the request body are changed.

## Authentication

Not required.

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Category ID to update. |

## Request Body

`application/json` with arbitrary field names (`Map<String, JsonElement?>`). Send only the fields that should change, for example `id` or `name`.

### Example

```json
{
  "name": "Lower limbs"
}
```

## Response

### Success

`200 OK`

Returns the updated `CategoryDto`.

### Response Body

```json
{
  "id": "1",
  "name": "Lower limbs"
}
```

## Notes

This endpoint is public and does not require a session or bearer token.