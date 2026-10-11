# GET /fiteo/api/1/categories

Gets all categories.

## Description

Returns the full list of exercise categories available in the system.

## Authentication

Not required.

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Identifier query parameter accepted by the route. |

## Response

### Success

`200 OK`

Returns a list of `CategoryDto` objects.

### Response Body

```json
[
  {
    "id": "1",
    "name": "Lower body"
  },
  {
    "id": "2",
    "name": "Upper body"
  }
]
```

## Notes

This endpoint is public and does not require a session or bearer token.