# GET /fiteo/api/1/exercises/page

Serves the exercises page.

## Description

Serves the static `index.html` resource that hosts the exercises page UI.

## Authentication

Not required.

## Response

### Success

`200 OK`

The response body is the HTML document for the exercises page (`text/html`).

### Response Body

```html
<!DOCTYPE html>
<html>
  <head>
    <title>Exercises</title>
  </head>
  <body>
    <h1>Exercises</h1>
  </body>
</html>
```

## Notes

This endpoint serves a static UI page rather than a JSON API payload.