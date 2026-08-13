# API Documentation (excluding fiteoApi and cheatDayApi)

This document summarizes the API endpoints implemented outside of the `fiteo` and `cheatday` modules — currently the ToDo API. All ToDo endpoints require authentication (session/bearer as configured).

## Authentication
Note: ToDo endpoints are protected using the configured authentication (session or bearer). Include appropriate Authorization headers or session cookies when calling.

---

## ToDo API
Base: /todo/api/1

### GET /todo/api/1/tasks
- Description: Get all tasks for authenticated user
- Auth: Required
- Request: none
- Response: 200 -> List<TaskDto>

### POST /todo/api/1/tasks
- Description: Create a new task
- Auth: Required
- Request body: CreateTaskDto
  - description: String
  - parentTaskId: Int? (optional)
  - priority: Int? (optional)
  - highestPriorityAsDefault: Boolean? (optional)
  - scheduler: SchedulerDto? (optional)
  - isToDo: Boolean? (optional)
  - expirationDate: String? (optional, ISO date-time)
  - shares: List<TaskShareDto>? (optional)
- Response: 200 -> TaskDto

### PATCH /todo/api/1/tasks/{id}
- Description: Update a task by id
- Auth: Required
- Path params:
  - id: Int
- Request body: UpdateTaskDto
  - description: String? (optional)
  - parentTaskId: NullableFieldDto<Int>? (use to set/clear parent)
  - priority: Int? (optional)
  - isToDo: Boolean? (optional)
  - scheduler: NullableFieldDto<SchedulerDto>? (optional)
  - expirationDate: NullableFieldDto<String>? (optional)
  - shares: List<UpdateTaskShareDto>? (optional)
- Response: 200 -> TaskDto

### DELETE /todo/api/1/tasks/{id}
- Description: Remove (delete) a task by id
- Auth: Required
- Path params:
  - id: Int
- Query params:
  - removeWithSubtasks: Boolean (optional, default: false) — whether to remove subtasks as well
- Response: 200 -> TaskDto

### GET /todo/api/1/tasks/{id}/copy
- Description: Copy a task (returns the copied task)
- Auth: Required
- Path params:
  - id: Int
- Response: 200 -> TaskDto

### POST /todo/api/1/tasks/mark-as-to-do
- Description: Mark multiple tasks as to-do or not
- Auth: Required
- Request body: MarkAsToDoDto
  - isToDo: Boolean
  - taskIds: List<Int>
- Response: 200 -> List<TaskDto>

### POST /todo/api/1/tasks/leave-share
- Description: Leave a shared task
- Auth: Required
- Request body: LeaveShareDto
  - id: Int
- Response: 200 -> TaskDto

---

## DTOs (summary)

### TaskDto
- id: Int
- ownerId: Int
- description: String
- addedTime: String (ISO date-time)
- modifiedTime: String (ISO date-time)
- parentTaskId: Int? (nullable)
- subTasks: List<TaskDto>
- isToDo: Boolean
- priority: Int
- scheduler: SchedulerDto? (nullable)
- expirationDate: String? (nullable, ISO date-time)
- shares: List<TaskShareDto>

### CreateTaskDto
(see fields under POST /tasks)

### UpdateTaskDto
(see fields under PATCH /tasks/{id})

### MarkAsToDoDto
- isToDo: Boolean
- taskIds: List<Int>

### LeaveShareDto
- id: Int

### SchedulerDto
- hour: Int
- minute: Int
- creationDate: String? (ISO date-time)
- startDate: String (ISO date-time)
- lastDate: String? (ISO date-time)
- daysOfWeek: List<Int>
- dayOfMonth: Int
- repeatCount: Int
- repeatInEveryPeriod: Int
- type: String
- options: Map<String,String>? (optional)

---

## Authorization API (fiteo)
Base: /fiteo/api/1

### POST /fiteo/api/1/registration
- Description: Register a new user
- Auth: Not required
- Request body: RegisterRequestDto
  - email: String
  - password: String
  - repeatedPassword: String
- Response: 200 -> UserDto (id, email)

### POST /fiteo/api/1/login
- Description: Login with email/password and establish session
- Auth: Not required
- Request body: LoginRequestDto
  - email: String?
  - password: String?
- Behavior: On success a session header is set (Authorization or Authorization-Test for test prefix) so subsequent requests use session-based auth.
- Response: 200 -> UserDto (id, email)

### POST /fiteo/api/1/login-bearer
- Description: Login and receive a bearer token in Authorization header
- Auth: Not required
- Request body: LoginRequestDto
- Behavior: On successful login the server creates a signed JWT and appends an Authorization header (value masked in server logs). Use returned token for bearer authentication.
- Response: 200 -> UserDto (id, email)

### GET /fiteo/api/1/user
- Description: Get current authenticated user (uses session authentication)
- Auth: Required (session)
- Request: none (session cookie/header used)
- Response: 200 -> UserDto

### GET /fiteo/api/1/users
- Description: Get all users (admin/test flows; requires appropriate auth)
- Auth: Required (session)
- Request: none
- Response: 200 -> List<UserDto>

### GET /fiteo/api/1/logout
- Description: Logout current user and clear session; also deletes FCM tokens server-side for the user
- Auth: Required (session)
- Request: none
- Response: 200 -> Unit (empty success)

---

If more modules should be included, say so and the document can be extended or split into multiple files.
