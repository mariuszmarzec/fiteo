# GET /fiteo/api/1/trainings

Gets all trainings for the authenticated user.

## Description

Returns the full list of trainings owned by the authenticated user, including the progress for each exercise.

## Authentication

Required (session).

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Identifier query parameter accepted by the route. |

## Response

### Success

`200 OK`

Returns a list of `TrainingDto` objects.

### Response Body

```json
[
  {
    "id": 1,
    "templateId": 1,
    "createDateInMillis": "2026-08-13T10:00:00Z",
    "finishDateInMillis": "2026-08-13T11:00:00Z",
    "exercisesWithProgress": [
      {
        "id": 1,
        "exerciseId": 1,
        "series": [
          {
            "seriesId": 1,
            "exerciseId": 1,
            "trainingId": 1,
            "date": "2026-08-13T10:00:00Z",
            "burden": 60.0,
            "timeInMillis": null,
            "repsNumber": 5,
            "note": "Warm up"
          }
        ],
        "trainingPartId": 1,
        "name": "Squat"
      }
    ]
  }
]
```

## Notes

This endpoint requires an authenticated session.