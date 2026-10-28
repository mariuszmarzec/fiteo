# GET /fiteo/api/1/trainings/templates

Gets all training templates for the authenticated user.

## Description

Returns the full list of training templates owned by the authenticated user, including the exercises and available equipment for each template.

## Authentication

Required (session).

## Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | String | No | Identifier query parameter accepted by the route. |

## Response

### Success

`200 OK`

Returns a list of `TrainingTemplateDto` objects.

### Response Body

```json
[
  {
    "id": 1,
    "name": "Lower body day",
    "exercises": [
      {
        "id": 1,
        "name": "Squat",
        "pinnedExercise": {
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
        },
        "categories": [
          {
            "id": "1",
            "name": "Lower body"
          }
        ],
        "excludedExercises": [],
        "excludedEquipment": []
      }
    ],
    "availableEquipment": [
      {
        "id": "1",
        "name": "Barbell"
      }
    ]
  }
]
```

## Notes

This endpoint requires an authenticated session.