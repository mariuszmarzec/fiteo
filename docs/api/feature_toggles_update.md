# PATCH /fiteo/api/1/feature_toggles/{id}

Updates an existing feature toggle by ID.

## Description

Partially updates the feature toggle identified by the supplied path parameter. Only the fields present in the request body are changed.

## Authentication

Not required.

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Feature toggle ID to update. |

## Request Body

`application/json` with arbitrary field names (`Map<String, JsonElement?>`). Send only the fields that should change, for example `name` or `value`.

### Example

```json
{
  "value": "false"
}
```

## Response

### Success

`200 OK`

Returns the updated `FeatureToggleDto`.

### Response Body

```json
{
  "id": 1,
  "name": "new_dashboard",
  "value": "false"
}
```

## Notes

This endpoint is public and does not require a session or bearer token.