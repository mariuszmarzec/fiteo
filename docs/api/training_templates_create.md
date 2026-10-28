# POST /fiteo/api/1/trainings/templates

Creates a new training template.

## Description

Creates a new training template with the supplied identifier, name, exercises and available equipment.

## Authentication

Required (session).

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Identifier query parameter accepted by the route. |

## Request Body

`CreateTrainingTemplateDto`

| Field | Type | Required | Description |
|---|---|---|---|
| id | Int | Yes | Template identifier. |
| name | String | Yes | Template name. |
| exercises | List<CreateTrainingTemplatePartDto> | Yes | Template exercises. |
| availableEquipmentIds | List<String> | Yes | IDs of equipment available for the template. |

### CreateTrainingTemplatePartDto

| Field | Type | Required | Description |
|---|---|---|---|
| id | Int? | No | Part identifier. |
| name | String | Yes | Part name. |
| pinnedExerciseId | Int? | No | ID of the pinned exercise for the part. |
| categoryIds | List<String> | Yes | Category IDs that select exercises for the part. |
| excludedExercisesIds | List<Int> | Yes | Exercise IDs excluded from the part. |
| excludedEquipmentIds | List<String> | Yes | Equipment IDs excluded from the part. |

### Example

```json
{
  "id": 1,
  "name": "Lower body day",
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

Returns the created `TrainingTemplateDto`.

### Response Body

```json
{
  "id": 1,
  "name": "Lower body day",
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