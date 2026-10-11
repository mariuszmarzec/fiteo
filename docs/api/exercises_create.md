# POST /fiteo/api/1/exercises

Creates a new exercise.

## Description

Creates a new exercise with the supplied name, media URLs, categories and required equipment.

## Authentication

Not required.

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Identifier query parameter accepted by the route. |

## Request Body

`CreateExerciseDto`

| Field | Type | Required | Description |
|---|---|---|---|
| name | String | Yes | Exercise name. |
| animationUrl | String? | No | URL of the exercise animation. |
| videoUrl | String? | No | URL of the exercise video. |
| category | List<CategoryDto> | Yes | Categories the exercise belongs to. |
| neededEquipment | List<EquipmentDto> | Yes | Equipment required to perform the exercise. |
| thumbnailUrl | String? | No | URL of the exercise thumbnail image. |

### CategoryDto

| Field | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Category identifier. |
| name | String | Yes | Category name. |

### EquipmentDto

| Field | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Equipment identifier. |
| name | String | Yes | Equipment name. |

### Example

```json
{
  "name": "Squat",
  "animationUrl": "https://example.com/squat.gif",
  "videoUrl": "https://example.com/squat.mp4",
  "category": [
    {
      "id": "1",
      "name": "Lower body"
    }
  ],
  "neededEquipment": [
    {
      "id": "1",
      "name": "Barbell"
    }
  ],
  "thumbnailUrl": "https://example.com/squat-thumb.png"
}
```

## Response

### Success

`200 OK`

Returns the created `ExerciseDto`.

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