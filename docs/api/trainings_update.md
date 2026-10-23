# PATCH /fiteo/api/1/trainings/{id}

Updates an existing training by ID.

## Description

Updates the training identified by the supplied path parameter, setting the
finish date and the progress for each exercise.

## Authentication

Required (session).

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Training ID to update. |

## Request Body

`UpdateTrainingDto`

| Field | Type | Required | Description |
|---|---|---|---|
| finishDateInMillis | String | Yes | Finish date in ISO date-time format. |
| exercisesWithProgress | List<UpdateTrainingExerciseWithProgressDto> | Yes | Updated exercise progress. |

### UpdateTrainingExerciseWithProgressDto

| Field | Type | Required | Description |
|---|---|---|---|
| id | Int? | No | Exercise progress identifier. |
| exerciseId | Int | Yes | Exercise identifier. |
| series | List<SeriesDto> | Yes | Series performed for the exercise. |
| trainingPartId | Int? | No | Training part identifier. |
| name | String | Yes | Exercise name. |

### SeriesDto

| Field | Type | Required | Description |
|---|---|---|---|
| seriesId | Int | Yes | Series identifier. |
| exerciseId | Int | Yes | Exercise identifier. |
| trainingId | Int | Yes | Training identifier. |
| date | String | Yes | Series date in ISO date-time format. |
| burden | Float? | No | Burden (weight) used. |
| timeInMillis | Int? | No | Duration in milliseconds. |
| repsNumber | Int? | No | Number of repetitions. |
| note | String | Yes | Free-form note. |

### Example

```json
{
  "finishDateInMillis": "2026-08-13T11:00:00",
  "exercisesWithProgress": [
    {
      "exerciseId": 1,
      "series": [
        {
          "seriesId": 1,
          "exerciseId": 1,
          "trainingId": 1,
          "date": "2026-08-13T10:30:00",
          "burden": 70.0,
          "repsNumber": 5,
          "note": "Working set"
        }
      ],
      "name": "Squat"
    }
  ]
}
```

## Response

### Success

`200 OK`

Returns the updated `TrainingDto`.

### Response Body

```json
{
  "id": 1,
  "templateId": 1,
  "createDateInMillis": "2026-08-13T10:00:00",
  "finishDateInMillis": "2026-08-13T11:00:00",
  "exercisesWithProgress": [
    {
      "id": 1,
      "exerciseId": 1,
      "series": [
        {
          "seriesId": 1,
          "exerciseId": 1,
          "trainingId": 1,
          "date": "2026-08-13T10:30:00",
          "burden": 70.0,
          "timeInMillis": null,
          "repsNumber": 5,
          "note": "Working set"
        }
      ],
      "trainingPartId": null,
      "name": "Squat"
    }
  ]
}
```

## Date-time format

API date-time values use `yyyy-MM-dd'T'HH:mm:ss` without a `Z` suffix or UTC offset, matching the backend's `Api.DATE_FORMAT`. The current parser does not accept timezone suffixes.

## Notes

This endpoint requires an authenticated session.