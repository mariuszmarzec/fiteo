# API Documentation

This directory contains human-readable documentation for every endpoint in the
generated OpenAPI contract (`src/jvmMain/resources/openapi.yaml`).

## Authentication

- [Registration](registration.md)
- [Login](login.md)
- [Login bearer](login_bearer.md)
- [Logout](logout.md)

## Users

- [Get current user](user_get.md)
- [Get users](users_get.md)

## Tasks

- [Get tasks](tasks_get.md)
- [Create task](tasks_create.md)
- [Update task](tasks_update.md)
- [Delete task](tasks_delete.md)
- [Copy task](tasks_copy.md)
- [Mark tasks as ToDo](tasks_mark_as_todo.md)
- [Leave shared task](tasks_leave_share.md)

## Trainings

- [Get trainings](trainings_get.md)
- [Create training](trainings_create.md)
- [Get training](trainings_get_by_id.md)
- [Delete training](trainings_delete.md)
- [Update training](trainings_update.md)

## Training templates

- [Get training templates](training_templates_get.md)
- [Create training template](training_templates_create.md)
- [Get training template](training_templates_get_by_id.md)
- [Delete training template](training_templates_delete.md)
- [Update training template](training_templates_update.md)

## Exercises

- [Get exercises](exercises_get.md)
- [Create exercise](exercises_create.md)
- [Get exercise](exercises_get_by_id.md)
- [Delete exercise](exercises_delete.md)
- [Update exercise](exercises_update.md)
- [Exercises page](exercises_page.md)

## Equipment

- [Get equipment](equipment_get.md)
- [Create equipment](equipment_create.md)
- [Get equipment](equipment_get_by_id.md)
- [Delete equipment](equipment_delete.md)
- [Update equipment](equipment_update.md)

## Categories

- [Get categories](categories_get.md)
- [Create category](categories_create.md)
- [Get category](categories_get_by_id.md)
- [Delete category](categories_delete.md)
- [Update category](categories_update.md)

## Feature toggles

- [Get feature toggles](feature_toggles_get.md)
- [Create feature toggle](feature_toggles_create.md)
- [Get feature toggle](feature_toggles_get_by_id.md)
- [Delete feature toggle](feature_toggles_delete.md)
- [Update feature toggle](feature_toggles_update.md)

## FCM tokens

- [Register FCM token](fcm_token_create.md)
- [Delete FCM token](fcm_token_delete.md)

## Cheat day (weights)

- [Get weights](weights_get.md)
- [Record weight](weights_create.md)
- [Get weight](weights_get_by_id.md)
- [Delete weight](weights_delete.md)
- [Update weight](weights_update.md)

## Server-Sent Events

- [SSE stream](sse.md)

## Scripts

These endpoints are exposed through the scripts panel and use HTTP Basic
Authentication (`auth-basic`). They are part of the running application but
are excluded from the generated OpenAPI document.

- [Run shopping list generator](scripts_run_a.md)
- [Clear list](scripts_run_b.md)
- [Generate median list](scripts_run_median.md)

## Runtime behavior

- [Runtime behavior not fully represented by OpenAPI](runtime-behavior.md)

## OpenAPI

- [Compile-time OpenAPI and Swagger UI](openapi.md)
