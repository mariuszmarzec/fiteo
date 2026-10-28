# DELETE /fiteo/api/1/categories/{id}

Deletes a category by ID.

## Description

Removes the category identified by the supplied path parameter.

## Authentication

Not required.

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Category ID to delete. |

## Response

### Success

`200 OK`

Returns the deleted `CategoryDto`.

### Response Body

```json
{
  "id": "1",
  "name": "Lower body"
}
```

## Notes

This endpoint is public and does not require a session or bearer token.