# DELETE /fiteo/api/1/trainings/{id}

Deletes a training by ID.

## Description

Removes the training identified by the supplied path parameter.

## Authentication

Required (session).

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Training ID to delete. |

## Response

### Success

`200 OK`

Returns the deleted `TrainingDto`.

### Response Body

```json
{
  "id": 1,
  "templateId": 1,
  "createDateInMillis": "2026-08-13T10:00:00",
  "finishDateInMillis": "2026-08-13T11:00:00",
  "exercisesWithProgress": []
}
```

## Date-time format

API date-time values use `yyyy-MM-dd'T'HH:mm:ss` without a `Z` suffix or UTC offset, matching the backend's `Api.DATE_FORMAT`. The current parser does not accept timezone suffixes.

## Notes

This endpoint requires an authenticated session.