# DELETE /fiteo/api/1/trainings/templates/{id}

Deletes a training template by ID.

## Description

Removes the training template identified by the supplied path parameter.

## Authentication

Required (session).

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Training template ID to delete. |

## Response

### Success

`200 OK`

Returns the deleted `TrainingTemplateDto`.

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