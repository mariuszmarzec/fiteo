# GET /fiteo/api/1/trainings/templates/{id}

Gets a single training template by ID.

## Description

Returns the training template identified by the supplied path parameter.

## Authentication

Required (session).

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Training template ID. |

## Response

### Success

`200 OK`

Returns the requested `TrainingTemplateDto`.

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