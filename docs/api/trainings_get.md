# GET /fiteo/api/1/trainings

Gets all trainings for the authenticated user.

## Description

Returns the trainings owned by the authenticated user, including the progress for each exercise.

This route is a single `GET /fiteo/api/1/trainings` endpoint that dispatches to two
different controller methods depending on the query parameters:

1. **Legacy (no pagination)** — when neither `limit` nor `cursor` query parameters are present, the route delegates to the untouched `getTrainings` method and returns a plain JSON array of `TrainingDto` objects ordered by `createDateInMillis DESC`. Existing callers keep working unchanged.
2. **Paginated** — when at least one of `limit` or `cursor` is present, the route delegates to the new `getPagingTrainings` method and returns a JSON object `{ "items": [...], "nextCursor": <int|null> }` ordered by `id DESC`. `cursor=C` filters trainings to those with `id <= C` (the cursor row itself is re-included on the next page, so no record is skipped). `limit` controls the page size (default 10, accepted range 1–100).

`?limit=20` starts the first page; `?cursor=123` continues the next page using the default limit of 10. `limit` does **not** have to be supplied together with `cursor`. `nextCursor` is the `id` of the first record of the following page (the extra row fetched with `limit + 1`), or `null` when the current page is the last one.

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