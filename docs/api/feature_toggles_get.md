# GET /fiteo/api/1/feature_toggles

Gets all feature toggles.

## Description

Returns the full list of feature toggles configured in the system.

## Authentication

Not required.

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Identifier query parameter accepted by the route. |

## Response

### Success

`200 OK`

Returns a list of `FeatureToggleDto` objects.

### Response Body

```json
[
  {
    "id": 1,
    "name": "new_dashboard",
    "value": "true"
  },
  {
    "id": 2,
    "name": "dark_mode",
    "value": "false"
  }
]
```
