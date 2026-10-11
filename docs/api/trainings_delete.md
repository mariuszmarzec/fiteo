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
  "createDateInMillis": "2026-08-13T10:00:00Z",
  "finishDateInMillis": "2026-08-13T11:00:00Z",
  "exercisesWithProgress": []
}
```

## Notes

This endpoint requires an authenticated session.