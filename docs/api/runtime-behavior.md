# Runtime behavior not fully represented by OpenAPI

This page records verified implementation behavior that is important to API clients but is not expressed reliably by the generated OpenAPI schema or examples.

It is a **maintained runtime-contract note**, not generated endpoint documentation. The API documentation sync script must preserve this file and use it as a mandatory reference when generating or updating endpoint pages. If implementation behavior changes, update this page after checking the source code.

## Task scheduler type values

The `SchedulerDto.type` field is case-sensitive. Its values are derived from the Kotlin scheduler class names:

| Exact API value | Meaning |
|---|---|
| `OneShot` | One-time schedule |
| `Weekly` | Weekly recurring schedule |
| `Monthly` | Monthly recurring schedule |

Use these exact values in requests and expect these values in responses. Lowercase values such as `weekly` are invalid: the backend maps the input string to a scheduler by exact class simple name and throws for unknown values.

When documenting task endpoints, include a `OneShot` example and do not infer allowed values solely from a single OpenAPI example.

## Task date-time format

The backend's shared `Api.DATE_FORMAT` is `yyyy-MM-dd'T'HH:mm:ss`. These fields are parsed/formatted as local date-time strings:

- `scheduler.startDate`
- `scheduler.creationDate`
- `scheduler.lastDate`
- `expirationDate`
- the date-time fields in task responses, including `addedTime` and `modifiedTime`

Example: `2026-08-13T09:00:00`.

Do **not** append `Z` or a UTC offset such as `+02:00`. These suffixes are not accepted by the current `LocalDateTime` parser. Keep JSON examples for these task endpoints consistent with this format, including response examples.

## Login and authentication headers

### Session login

`POST /fiteo/api/1/login` establishes the regular session. The response's `Authorization` header carries the authentication value. Clients must read that header and reuse the **entire header value unchanged** in the `Authorization` header of subsequent authenticated requests. The JSON response body contains the user record, not the token.

Do not describe this behavior vaguely as an unspecified cookie-or-header choice, and do not attempt to reconstruct the header value.

### Explicit bearer login

`POST /fiteo/api/1/login-bearer` is a separate flow. Its `Authorization` response header contains `Bearer <token>`; reuse the full value as returned. The current implementation sets the JWT expiry to 60 seconds.

Keep session login and explicit bearer login clearly distinguished in endpoint documentation.
