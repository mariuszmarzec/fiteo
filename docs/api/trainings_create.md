# POST /fiteo/api/1/trainings

Creates a new training from a training template.

## Description

Creates a new training based on the supplied training template identifier.

## Authentication

Required (session).

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Identifier query parameter accepted by the route. |

## Request Body

`CreateTrainingDto`

| Field | Type | Required | Description |
|---|---|---|---|
| templateTrainingId | Int | Yes | ID of the training template to base the new training on. |

### Example

```json
{
  "templateTrainingId": 1
}
```

## Response

### Success

`200 OK`

Returns the created `TrainingDto`.

### Response Body

```json
{
  "id": 1,
  "templateId": 1,
  "createDateInMillis": "2026-08-13T10:00:00Z",
  "finishDateInMillis": "2026-08-13T11:00:00Z",
  "exercisesWithProgress": []
}
```

## Notes

This endpoint requires an authenticated session.