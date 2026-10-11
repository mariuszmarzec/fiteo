# GET /fiteo/api/1/exercises

Gets all exercises.

## Description

Returns the full list of exercises available in the system, including their categories and required equipment.

## Authentication

Not required.

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Identifier query parameter accepted by the route. |

## Response

### Success

`200 OK`

Returns a list of `ExerciseDto` objects.

### Response Body

```json
[
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
    "imagesNames": ["squat-front.png"],
    "imagesUrls": ["https://example.com/squat-front.png"],
    "descriptionsToImages": ["Front view"],
    "imagesMistakesUrls": [],
    "imagesMistakesNames": [],
    "descriptionsToMistakes": [],
    "muscles": ["quadriceps"],
    "musclesName": ["Quadriceps"],
    "neededEquipment": [
      {
        "id": "1",
        "name": "Barbell"
      }
    ],
    "thumbnailName": "squat-thumb.png",
    "thumbnailUrl": "https://example.com/squat-thumb.png"
  }
]
```

## Notes

This endpoint is public and does not require a session or bearer token.