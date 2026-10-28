# PATCH /fiteo/api/1/exercises/{id}

Updates an existing exercise by ID.

## Description

Partially updates the exercise identified by the supplied path parameter. Only the fields present in the request body are changed.

## Authentication

Not required.

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Exercise ID to update. |

## Request Body

`application/json` with arbitrary field names (`Map<String, JsonElement?>`). Send only the fields that should change, for example `name`, `animationUrl`, `videoUrl`, `category`, `neededEquipment` or `thumbnailUrl`.

### Example

```json
{
  "name": "Barbell Squat",
  "thumbnailUrl": "https://example.com/squat-thumb-v2.png"
}
```

## Response

### Success

`200 OK`

Returns the updated `ExerciseDto`.

### Response Body

```json
{
  "id": 1,
  "name": "Barbell Squat",
  "animationImageName": "squat.gif",
  "animationUrl": "https://example.com/squat.gif",
  "videoUrl": "https://example.com/squat.mp4",
  "category": [
    {
      "id": "1",
      "name": "Lower body"
    }
  ],
  "imagesNames": null,
  "imagesUrls": null,
  "descriptionsToImages": null,
  "imagesMistakesUrls": null,
  "imagesMistakesNames": null,
  "descriptionsToMistakes": null,
  "muscles": null,
  "musclesName": null,
  "neededEquipment": [
    {
      "id": "1",
      "name": "Barbell"
    }
  ],
  "thumbnailName": "squat-thumb-v2.png",
  "thumbnailUrl": "https://example.com/squat-thumb-v2.png"
}
```

## Notes

This endpoint is public and does not require a session or bearer token.