# DELETE /fiteo/api/1/feature_toggles/{id}

Deletes a feature toggle by ID.

## Description

Removes the feature toggle identified by the supplied path parameter.

## Authentication

Not required.

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Feature toggle ID to delete. |

## Response

### Success

`200 OK`

Returns the deleted `FeatureToggleDto`.

### Response Body

```json
{
  "id": 1,
  "name": "new_dashboard",
  "value": "true"
}
```

## Notes

This endpoint is public and does not require a session or bearer token.