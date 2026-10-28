# POST /fiteo/api/1/categories

Creates a new category.

## Description

Creates a new exercise category with the supplied identifier and name.

## Authentication

Not required.

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Identifier query parameter accepted by the route. |

## Request Body

`CategoryDto`

| Field | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Category identifier. |
| name | String | Yes | Category name. |

### Example

```json
{
  "id": "1",
  "name": "Lower body"
}
```

## Response

### Success

`200 OK`

Returns the created `CategoryDto`.

### Response Body

```json
{
  "id": "1",
  "name": "Lower body"
}
```

## Notes

This endpoint is public and does not require a session or bearer token.