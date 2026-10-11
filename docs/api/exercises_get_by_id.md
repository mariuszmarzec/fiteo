# GET /fiteo/api/1/exercises/{id}

Gets a single exercise by ID.

## Description

Returns the exercise identified by the supplied path parameter, including its categories and required equipment.

## Authentication

Not required.

## Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Exercise ID. |

## Response

### Success

`200 OK`

Returns the requested `ExerciseDto`.

### Response Body

```json
{
  "id": 1,
  "name": "Squat",
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
  "thumbnailName": "squat-thumb.png",
  "thumbnailUrl": "https://example.com/squat-thumb.png"
}
```

## Notes

This endpoint is public and does not require a session or bearer token.