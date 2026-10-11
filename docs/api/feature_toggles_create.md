# POST /fiteo/api/1/feature_toggles

Creates a new feature toggle.

## Description

Creates a new feature toggle with the supplied name and value.

## Authentication

Not required.

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Identifier query parameter accepted by the route. |

## Request Body

`NewFeatureToggleDto`

| Field | Type | Required | Description |
|---|---|---|---|
| name | String | Yes | Feature toggle name. |
| value | String | Yes | Feature toggle value. |

### Example

```json
{
  "name": "new_dashboard",
  "value": "true"
}
```

## Response

### Success

`200 OK`

Returns the created `FeatureToggleDto`.

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