# GET /fiteo/api/1/trainings/{id}

Gets a single training by ID.

## Description

Returns the training identified by the supplied path parameter, including the progress for each exercise.

## Authentication

Required (session).

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Training ID. |

## Response

### Success

`200 OK`

Returns the requested `TrainingDto`.

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
          "date": "2026-08-13T10:00:00",
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
```

## Date-time format

API date-time values use `yyyy-MM-dd'T'HH:mm:ss` without a `Z` suffix or UTC offset, matching the backend's `Api.DATE_FORMAT`. The current parser does not accept timezone suffixes.

## Notes

This endpoint requires an authenticated session.