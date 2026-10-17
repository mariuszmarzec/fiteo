# GET /fiteo/api/1/trainings

Gets all trainings for the authenticated user.

## Description

Returns the trainings owned by the authenticated user, including the progress for each exercise.

The endpoint supports two response contracts:

1. **Legacy (no pagination)** — when neither `limit` nor `cursor` query parameters are present, the response is a plain JSON array of `TrainingDto` objects ordered by `createDateInMillis DESC`.
2. **Paginated** — when at least one of `limit` or `cursor` is present, the response is a JSON object `{ "items": [...], "nextCursor": <int|null> }` ordered by `id DESC`. `cursor=C` filters trainings to those with `id < C`. `limit` controls the page size (default 10, accepted range 1–100).

## Authentication

Required (session).

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Identifier query parameter accepted by the route. |
| limit | Integer | No | Page size (1–100). Defaults to 10. When present alone it starts the first page of the paginated response. When present together with `cursor` it applies to that page. An empty value (`?limit=`) returns `400 Bad Request`. |
| cursor | Integer | No | Cursor returned by a previous response. Filters trainings to those with `id < cursor`. When present alone it continues the next page using the default limit of 10. An empty value (`?cursor=`) returns `400 Bad Request`. |

## Response

### Success

`200 OK`

Returns either a list of `TrainingDto` objects (legacy contract) or a `TrainingsPageDto` object (paginated contract).

### Response Body — Legacy

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

### Response Body — Paginated

```json
{
  "items": [
    {
      "id": 1,
      "templateId": 1,
      "createDateInMillis": "2026-08-13T10:00:00Z",
      "finishDateInMillis": "2026-08-13T11:00:00Z",
      "exercisesWithProgress": []
    }
  ],
  "nextCursor": null
}
```

### Error

`400 Bad Request` — returned when `limit` is not an integer in the 1–100 range (or is empty), or when `cursor` is not a positive integer (or is empty).

## Notes

This endpoint requires an authenticated session.