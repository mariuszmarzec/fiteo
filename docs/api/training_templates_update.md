# PATCH /fiteo/api/1/trainings/templates/{id}

Updates an existing training template by ID.

## Description

Partially updates the training template identified by the supplied path parameter. The request body uses the same structure as the create operation.

## Authentication

Required (session).

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Training template ID to update. |

## Request Body

`CreateTrainingTemplateDto`

| Field | Type | Required | Description |
|---|---|---|---|
| id | Int | Yes | Template identifier. |
| name | String | Yes | Template name. |
| exercises | List<CreateTrainingTemplatePartDto> | Yes | Template exercises. |
| availableEquipmentIds | List<String> | Yes | IDs of equipment available for the template. |

### Example

```json
{
  "id": 1,
  "name": "Full body day",
  "exercises": [
    {
      "name": "Squat",
      "pinnedExerciseId": 1,
      "categoryIds": ["1"],
      "excludedExercisesIds": [],
      "excludedEquipmentIds": []
    }
  ],
  "availableEquipmentIds": ["1"]
}
```

## Response

### Success

`200 OK`

Returns the updated `TrainingTemplateDto`.

### Response Body

```json
{
  "id": 1,
  "name": "Full body day",
  "exercises": [
    {
      "id": 1,
      "name": "Squat",
      "pinnedExercise": null,
      "categories": [
        {
          "id": "1",
          "name": "Lower body"
        }
      ],
      "excludedExercises": [],
      "excludedEquipment": []
    }
  ],
  "availableEquipment": [
    {
      "id": "1",
      "name": "Barbell"
    }
  ]
}
```

## Notes

This endpoint requires an authenticated session.