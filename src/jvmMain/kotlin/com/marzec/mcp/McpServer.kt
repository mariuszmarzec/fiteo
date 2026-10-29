package com.marzec.mcp

import com.marzec.Api
import com.marzec.exceptions.HttpException
import com.marzec.exceptions.HttpStatus
import com.marzec.fiteo.model.domain.User
import com.marzec.cheatday.CheatDayController
import com.marzec.cheatday.dto.PutWeightDto
import com.marzec.cheatday.dto.WeightDto
import com.marzec.fiteo.model.http.HttpResponse
import com.marzec.fiteo.services.PersonalAccessTokenService
import com.marzec.todo.ToDoApiController
import com.marzec.todo.dto.TaskDto
import com.marzec.todo.model.CreateTaskDto
import com.marzec.todo.model.MarkAsToDoDto
import com.marzec.todo.model.SchedulerDto
import com.marzec.todo.model.TaskShareDto
import com.marzec.todo.model.UpdateTaskDto
import com.marzec.todo.model.LeaveShareDto
import com.marzec.todo.model.toDomain
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.request.path
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcpStatelessStreamableHttp
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.McpJson
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

internal const val MCP_ENDPOINT_PATH = "/mcp"

internal fun createMcpServer(
    requestContext: McpRequestContext,
    todoController: ToDoApiController,
    cheatDayController: CheatDayController? = null,
): Server = Server(
    serverInfo = Implementation(
        name = "fiteo",
        version = "1.0.0",
    ),
    options = ServerOptions(
        capabilities = ServerCapabilities(
            tools = ServerCapabilities.Tools(listChanged = false),
        ),
    ),
).apply {
    addJsonTool(
        name = "todo_list_tasks",
        description = "List ToDo tasks belonging to the owner of the authenticated PAT. Returns a JSON array of TaskDto objects. This tool accepts no arguments.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {},
            required = emptyList(),
        ),
        serializer = ListSerializer(TaskDto.serializer()),
        invalidArgumentsMessage = "Invalid todo_list_tasks arguments",
        internalErrorMessage = "Unable to list tasks because of an internal error.",
    ) { arguments ->
        val httpRequest = requestContext.createRequest(data = Unit, arguments = arguments)
        if (arguments.isNotEmpty()) {
            throw HttpException("todo_list_tasks does not accept arguments", HttpStatus.BAD_REQUEST)
        }
        todoController.getTasks(httpRequest)
    }

    addJsonTool(
        name = "todo_create_task",
        description = "Create a task for the owner of the authenticated PAT using existing ToDo rules. Scheduled tasks cannot have a parent. Optional shares are granted to the specified users. Returns the created TaskDto.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("task", createTaskDtoSchema())
            },
            required = listOf("task"),
        ),
        serializer = TaskDto.serializer(),
        invalidArgumentsMessage = "Invalid todo_create_task arguments. Check field types, date-time formats, scheduler type, and share permissions.",
        internalErrorMessage = "Unable to create task because of an internal error.",
    ) { arguments ->
        requestContext.createRequest(data = Unit, arguments = arguments)
        val input = decodeCreateTaskArguments(arguments)
        input.toDomain()
        todoController.addTask(requestContext.createRequest(data = input, arguments = arguments))
    }

    addJsonTool(
        name = "todo_update_task",
        description = "Update a task owned by the authenticated PAT. Supply id and a task object matching UpdateTaskDto. Only supplied fields are changed. For parentTaskId, scheduler and expirationDate use {\"value\": ...}; set value to null to clear that field. Scheduled tasks cannot have a parent, and a task cannot be its own parent. Returns the updated TaskDto.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("id", buildJsonObject {
                    put("type", "integer")
                    put("description", "ID of the task to update.")
                })
                put("task", updateTaskDtoSchema())
            },
            required = listOf("id", "task"),
        ),
        serializer = TaskDto.serializer(),
        invalidArgumentsMessage = "Invalid todo_update_task arguments. Check the task ID, partial-update wrappers, date-time formats, scheduler type and share permissions.",
        internalErrorMessage = "Unable to update task because of an internal error.",
    ) { arguments ->
        requestContext.createRequest(data = Unit, arguments = arguments)
        val (taskId, input) = decodeUpdateTaskArguments(arguments)
        input.toDomain()
        todoController.updateTask(
            requestContext.createRequest(
                data = input,
                arguments = arguments,
                parameters = mapOf(Api.Args.ARG_ID to taskId.toString()),
            ),
        )
    }

    addJsonTool(
        name = "todo_mark_tasks",
        description = "Set each supplied task ID to ToDo or not-ToDo using the existing permissions of the authenticated PAT owner. The call fails if any task cannot be edited by that user. Returns the task list visible to the PAT owner after the change.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("isToDo", buildJsonObject { put("type", "boolean") })
                put("taskIds", buildJsonObject {
                    put("type", "array")
                    put("minItems", 1)
                    put("items", buildJsonObject {
                        put("type", "integer")
                        put("minimum", 1)
                    })
                })
            },
            required = listOf("isToDo", "taskIds"),
        ),
        serializer = ListSerializer(TaskDto.serializer()),
        invalidArgumentsMessage = "Invalid todo_mark_tasks arguments. Supply a boolean isToDo and a non-empty array of positive integer task IDs.",
        internalErrorMessage = "Unable to update task status because of an internal error.",
    ) { arguments ->
        requestContext.createRequest(data = Unit, arguments = arguments)
        val input = decodeMarkAsToDoArguments(arguments)
        todoController.markAsToDo(requestContext.createRequest(data = input, arguments = arguments))
    }
    addJsonTool(
        name = "todo_copy_task",
        description = "Create and return a copy of the task identified by id for the authenticated PAT owner. This operation creates a new task; it is not a read-only lookup. Tasks the owner cannot access cannot be copied.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("id", buildJsonObject {
                    put("type", "integer")
                    put("minimum", 1)
                })
            },
            required = listOf("id"),
        ),
        serializer = TaskDto.serializer(),
        invalidArgumentsMessage = "Invalid todo_copy_task arguments. Supply a positive integer task ID.",
        internalErrorMessage = "Unable to copy task because of an internal error.",
    ) { arguments ->
        requestContext.createRequest(data = Unit, arguments = arguments)
        val taskId = decodeCopyTaskArguments(arguments)
        todoController.copyTasks(
            requestContext.createRequest(
                data = Unit,
                arguments = arguments,
                parameters = mapOf(Api.Args.ARG_ID to taskId.toString()),
            ),
        )
    }

    addJsonTool(
        name = "todo_delete_task",
        description = "Destructive operation: permanently delete the task identified by id for the authenticated PAT owner. By default, subtasks are not removed; set removeWithSubtasks to true to remove them as well. Tasks the owner cannot access cannot be deleted.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("id", buildJsonObject {
                    put("type", "integer")
                    put("minimum", 1)
                })
                put("removeWithSubtasks", buildJsonObject { put("type", "boolean") })
            },
            required = listOf("id"),
        ),
        serializer = TaskDto.serializer(),
        invalidArgumentsMessage = "Invalid todo_delete_task arguments. Supply a positive integer task ID and an optional boolean removeWithSubtasks.",
        internalErrorMessage = "Unable to delete task because of an internal error.",
    ) { arguments ->
        requestContext.createRequest(data = Unit, arguments = arguments)
        val (taskId, removeWithSubtasks) = decodeDeleteTaskArguments(arguments)
        todoController.removeTask(
            requestContext.createRequest(
                data = Unit,
                arguments = arguments,
                parameters = mapOf(Api.Args.ARG_ID to taskId.toString()),
                queries = removeWithSubtasks?.let {
                    mapOf("removeWithSubtasks" to listOf(it.toString()))
                } ?: emptyMap(),
            ),
        )
    }

    addJsonTool(
        name = "todo_leave_shared_task",
        description = "Leave a task shared with the authenticated PAT owner. This removes the owner's share and may change or remove their access to that task; it does not act on behalf of the task owner or another user.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("id", buildJsonObject {
                    put("type", "integer")
                    put("minimum", 1)
                })
            },
            required = listOf("id"),
        ),
        serializer = TaskDto.serializer(),
        invalidArgumentsMessage = "Invalid todo_leave_shared_task arguments. Supply a positive integer task ID.",
        internalErrorMessage = "Unable to leave shared task because of an internal error.",
    ) { arguments ->
        requestContext.createRequest(data = Unit, arguments = arguments)
        val input = decodeLeaveShareArguments(arguments)
        todoController.leaveShare(requestContext.createRequest(data = input, arguments = arguments))
    }

    cheatDayController?.let { weightController ->
        addJsonTool(
            name = "cheatday_list_weights",
            description = "List weight entries belonging to the owner of the authenticated PAT. Returns a JSON array of WeightDto objects. This tool accepts no arguments.",
            inputSchema = ToolSchema(
                properties = buildJsonObject {},
                required = emptyList(),
            ),
            serializer = ListSerializer(WeightDto.serializer()),
            invalidArgumentsMessage = "Invalid cheatday_list_weights arguments",
            internalErrorMessage = "Unable to list weights because of an internal error.",
        ) { arguments ->
            requestContext.createRequest(data = Unit, arguments = arguments)
            if (arguments.isNotEmpty()) {
                throw HttpException("cheatday_list_weights does not accept arguments", HttpStatus.BAD_REQUEST)
            }
            weightController.getWeights(requestContext.createRequest(data = Unit, arguments = arguments))
        }

        addJsonTool(
            name = "cheatday_get_weight",
            description = "Get a single weight entry by ID for the owner of the authenticated PAT. Entries belonging to other users are not accessible.",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    put("id", buildJsonObject {
                        put("type", "integer")
                        put("minimum", 1)
                    })
                },
                required = listOf("id"),
            ),
            serializer = WeightDto.serializer(),
            invalidArgumentsMessage = "Invalid cheatday_get_weight arguments. Supply a positive integer weight ID.",
            internalErrorMessage = "Unable to get weight because of an internal error.",
        ) { arguments ->
            requestContext.createRequest(data = Unit, arguments = arguments)
            val weightId = decodeGetWeightArguments(arguments)
            weightController.getWeight(
                requestContext.createRequest(
                    data = Unit,
                    arguments = arguments,
                    parameters = mapOf(Api.Args.ARG_ID to weightId.toString()),
                ),
            )
        }

        addJsonTool(
            name = "cheatday_create_weight",
            description = "Create a weight entry for the owner of the authenticated PAT. Supply the numeric value and a local date-time without a timezone suffix. This operation creates a record and returns its WeightDto.",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    put("value", buildJsonObject {
                        put("type", "number")
                        put("description", "Weight value as a finite JSON number.")
                    })
                    put("date", buildJsonObject {
                        put("type", "string")
                        put("description", "Local date-time, typically yyyy-MM-dd'T'HH:mm:ss, with no timezone suffix.")
                    })
                },
                required = listOf("value", "date"),
            ),
            serializer = WeightDto.serializer(),
            invalidArgumentsMessage = "Invalid cheatday_create_weight arguments. Supply a finite numeric value and a valid local date-time without a timezone suffix.",
            internalErrorMessage = "Unable to create weight because of an internal error.",
        ) { arguments ->
            val input = decodeCreateWeightArguments(arguments)
            weightController.putWeight(requestContext.createRequest(data = input, arguments = arguments))
        }
    }

}

/**
 * Registers PAT authentication before the SDK's Streamable HTTP handler and retains the
 * verified owner's request context only on the current Ktor call.
 */
internal fun Application.configureMcpEndpoint(
    personalAccessTokenService: () -> PersonalAccessTokenService,
    todoController: () -> ToDoApiController,
    cheatDayController: (() -> CheatDayController)? = null,
) {
    intercept(ApplicationCallPipeline.Plugins) {
        if (call.request.path() == MCP_ENDPOINT_PATH) {
            val owner = call.verifiedMcpPatOwner(personalAccessTokenService)
            if (owner == null) {
                call.response.header(HttpHeaders.WWWAuthenticate, "Bearer")
                call.response.header(HttpHeaders.CacheControl, "no-store")
                call.respond(HttpStatusCode.Unauthorized)
                finish()
            } else {
                call.attributes.put(McpRequestContextAttributeKey, McpRequestContext.fromVerifiedPatOwner(owner))
            }
        }
    }
    mcpStatelessStreamableHttp(path = MCP_ENDPOINT_PATH, enableDnsRebindingProtection = false) {
        val requestContext = call.attributes.getOrNull(McpRequestContextAttributeKey)
            ?: error("MCP request context missing after PAT authentication")
        createMcpServer(requestContext, todoController(), cheatDayController?.invoke())
    }
}

private fun createTaskDtoSchema(): JsonObject = buildJsonObject {
    put("type", "object")
    put("description", "Task payload matching CreateTaskDto.")
    put("properties", buildJsonObject {
        put("description", buildJsonObject { put("type", "string"); put("description", "Non-empty task description.") })
        put("parentTaskId", buildJsonObject { put("type", typeUnion("integer", "null")) })
        put("priority", buildJsonObject { put("type", typeUnion("integer", "null")) })
        put("highestPriorityAsDefault", buildJsonObject { put("type", typeUnion("boolean", "null")) })
        put("isToDo", buildJsonObject { put("type", typeUnion("boolean", "null")) })
        put("expirationDate", buildJsonObject { put("type", typeUnion("string", "null")); put("description", "yyyy-MM-dd'T'HH:mm:ss") })
        put("scheduler", buildJsonObject {
            put("type", typeUnion("object", "null"))
            put("properties", buildJsonObject {
                put("hour", buildJsonObject { put("type", "integer") })
                put("minute", buildJsonObject { put("type", "integer") })
                put("creationDate", buildJsonObject { put("type", typeUnion("string", "null")) })
                put("startDate", buildJsonObject { put("type", "string") })
                put("lastDate", buildJsonObject { put("type", typeUnion("string", "null")) })
                put("daysOfWeek", buildJsonObject { put("type", "array",); put("items", buildJsonObject { put("type", "integer") }) })
                put("dayOfMonth", buildJsonObject { put("type", "integer") })
                put("repeatCount", buildJsonObject { put("type", "integer") })
                put("repeatInEveryPeriod", buildJsonObject { put("type", "integer") })
                put("type", buildJsonObject {
                    put("type", "string")
                    put("enum", buildJsonArray { add(JsonPrimitive("OneShot")); add(JsonPrimitive("Weekly")); add(JsonPrimitive("Monthly")) })
                })
                put("options", buildJsonObject { put("type", typeUnion("object", "null")); put("additionalProperties", buildJsonObject { put("type", "string") }) })
            })
            put("required", buildJsonArray {
                add(JsonPrimitive("hour")); add(JsonPrimitive("minute")); add(JsonPrimitive("startDate"))
                add(JsonPrimitive("daysOfWeek")); add(JsonPrimitive("dayOfMonth")); add(JsonPrimitive("repeatCount"))
                add(JsonPrimitive("repeatInEveryPeriod")); add(JsonPrimitive("type"))
            })
            put("additionalProperties", false)
        })
        put("shares", buildJsonObject {
            put("type", typeUnion("array", "null"))
            put("items", buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {
                    put("userId", buildJsonObject { put("type", "string") })
                    put("permission", buildJsonObject {
                        put("type", "string")
                        put("enum", buildJsonArray { add(JsonPrimitive("VIEWER")); add(JsonPrimitive("EDITOR_AND_VIEWER")) })
                    })
                })
                put("required", buildJsonArray { add(JsonPrimitive("userId")); add(JsonPrimitive("permission")) })
                put("additionalProperties", false)
            })
        })
    })
    put("required", buildJsonArray { add(JsonPrimitive("description")) })
    put("additionalProperties", false)
}

private fun decodeCreateTaskArguments(arguments: JsonObject): CreateTaskDto {
    require(arguments.keys == setOf("task")) {
        "todo_create_task expects a single task object"
    }
    val taskArguments = arguments["task"] as? JsonObject
        ?: throw IllegalArgumentException("task must be an object")
    val allowed = setOf(
        "description", "parentTaskId", "priority", "highestPriorityAsDefault",
        "isToDo", "expirationDate", "scheduler", "shares",
    )
    require(taskArguments.keys.all { it in allowed }) { "Unknown task fields are not supported" }

    val descriptionValue = taskArguments["description"] as? JsonPrimitive
    require(descriptionValue != null && descriptionValue.isString && descriptionValue.content.isNotBlank()) {
        "task.description must be a non-empty string"
    }

    fun intField(name: String): Int? {
        val value = taskArguments[name] ?: return null
        if (value == JsonNull) return null
        return (value as? JsonPrimitive)?.intOrNull
            ?: throw IllegalArgumentException("task.$name must be an integer or null")
    }
    fun boolField(name: String): Boolean? {
        val value = taskArguments[name] ?: return null
        if (value == JsonNull) return null
        return (value as? JsonPrimitive)?.booleanOrNull
            ?: throw IllegalArgumentException("task.$name must be a boolean or null")
    }
    fun stringField(name: String): String? {
        val value = taskArguments[name] ?: return null
        if (value == JsonNull) return null
        val primitive = value as? JsonPrimitive
        require(primitive != null && primitive.isString) { "task.$name must be a string or null" }
        return primitive.content
    }

    val schedulerElement: JsonElement? = taskArguments["scheduler"]
    val scheduler = if (schedulerElement == null || schedulerElement == JsonNull) null else
        McpJson.decodeFromJsonElement(SchedulerDto.serializer(), schedulerElement)
    val sharesElement: JsonElement? = taskArguments["shares"]
    val shares = if (sharesElement == null || sharesElement == JsonNull) null else {
        sharesElement.jsonArray.map { item ->
            val share = item.jsonObject
            require(share.keys == setOf("userId", "permission")) {
                "Each task share must contain only userId and permission"
            }
            TaskShareDto(
                userId = (share["userId"] as? JsonPrimitive)?.takeIf { it.isString }?.content
                    ?: throw IllegalArgumentException("share.userId must be a string"),
                permission = (share["permission"] as? JsonPrimitive)?.takeIf { it.isString }?.content
                    ?: throw IllegalArgumentException("share.permission must be a string"),
            ).also {
                require(it.userId.toIntOrNull() != null) { "share.userId must be a numeric user ID" }
                require(it.permission in setOf("VIEWER", "EDITOR_AND_VIEWER")) {
                    "share.permission is unsupported"
                }
            }
        }
    }

    val expirationDate = stringField("expirationDate")
    require(expirationDate == null || runCatching { kotlinx.datetime.LocalDateTime.parse(expirationDate) }.isSuccess) {
        "task.expirationDate must use yyyy-MM-dd'T'HH:mm:ss"
    }
    return CreateTaskDto(
        description = descriptionValue.content,
        parentTaskId = intField("parentTaskId"),
        priority = intField("priority"),
        highestPriorityAsDefault = boolField("highestPriorityAsDefault"),
        isToDo = boolField("isToDo"),
        expirationDate = expirationDate,
        scheduler = scheduler,
        shares = shares,
    )
}


private fun updateTaskDtoSchema(): JsonObject = buildJsonObject {
    put("type", "object")
    put("description", "Task payload matching UpdateTaskDto. Omit fields that should remain unchanged.")
    put("properties", buildJsonObject {
        put("description", buildJsonObject { put("type", typeUnion("string", "null")) })
        put("parentTaskId", nullableWrapperSchema(buildJsonObject {
            put("type", typeUnion("integer", "null"))
            put("description", "Parent task ID; value null clears the parent.")
        }))
        put("priority", buildJsonObject { put("type", typeUnion("integer", "null")) })
        put("isToDo", buildJsonObject { put("type", typeUnion("boolean", "null")) })
        put("scheduler", nullableWrapperSchema(schedulerDtoSchema()))
        put("expirationDate", nullableWrapperSchema(buildJsonObject {
            put("type", typeUnion("string", "null"))
            put("description", "Local date-time in yyyy-MM-dd'T'HH:mm:ss format; value null clears the expiration date.")
        }))
        put("shares", buildJsonObject {
            put("type", typeUnion("array", "null"))
            put("description", "Replacement list of task shares; an empty array clears shares.")
            put("items", buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {
                    put("userId", buildJsonObject { put("type", "string") })
                    put("permission", buildJsonObject {
                        put("type", "string")
                        put("enum", buildJsonArray {
                            add(JsonPrimitive("VIEWER"))
                            add(JsonPrimitive("EDITOR_AND_VIEWER"))
                        })
                    })
                })
                put("required", buildJsonArray {
                    add(JsonPrimitive("userId"))
                    add(JsonPrimitive("permission"))
                })
                put("additionalProperties", false)
            })
        })
    })
    put("additionalProperties", false)
}

private fun nullableWrapperSchema(valueSchema: JsonObject): JsonObject = buildJsonObject {
    put("type", typeUnion("object", "null"))
    put("properties", buildJsonObject { put("value", valueSchema) })
    put("required", buildJsonArray { add(JsonPrimitive("value")) })
    put("additionalProperties", false)
}

private fun schedulerDtoSchema(): JsonObject = buildJsonObject {
    put("type", typeUnion("object", "null"))
    put("properties", buildJsonObject {
        put("hour", buildJsonObject { put("type", "integer") })
        put("minute", buildJsonObject { put("type", "integer") })
        put("creationDate", buildJsonObject { put("type", typeUnion("string", "null")) })
        put("startDate", buildJsonObject { put("type", "string") })
        put("lastDate", buildJsonObject { put("type", typeUnion("string", "null")) })
        put("daysOfWeek", buildJsonObject {
            put("type", "array")
            put("items", buildJsonObject { put("type", "integer") })
        })
        put("dayOfMonth", buildJsonObject { put("type", "integer") })
        put("repeatCount", buildJsonObject { put("type", "integer") })
        put("repeatInEveryPeriod", buildJsonObject { put("type", "integer") })
        put("type", buildJsonObject {
            put("type", "string")
            put("enum", buildJsonArray {
                add(JsonPrimitive("OneShot"))
                add(JsonPrimitive("Weekly"))
                add(JsonPrimitive("Monthly"))
            })
        })
        put("options", buildJsonObject {
            put("type", typeUnion("object", "null"))
            put("additionalProperties", buildJsonObject { put("type", "string") })
        })
    })
    put("required", buildJsonArray {
        add(JsonPrimitive("hour"))
        add(JsonPrimitive("minute"))
        add(JsonPrimitive("startDate"))
        add(JsonPrimitive("daysOfWeek"))
        add(JsonPrimitive("dayOfMonth"))
        add(JsonPrimitive("repeatCount"))
        add(JsonPrimitive("repeatInEveryPeriod"))
        add(JsonPrimitive("type"))
    })
    put("additionalProperties", false)
}

private fun decodeUpdateTaskArguments(arguments: JsonObject): Pair<Int, UpdateTaskDto> {
    require(arguments.keys == setOf("id", "task")) {
        "todo_update_task expects id and a nested task object"
    }
    val taskId = (arguments["id"] as? JsonPrimitive)?.intOrNull
        ?: throw IllegalArgumentException("id must be an integer")
    require(taskId > 0) { "id must be greater than zero" }

    val taskArguments = arguments["task"] as? JsonObject
        ?: throw IllegalArgumentException("task must be an object")
    val allowedFields = setOf(
        "description", "parentTaskId", "priority", "isToDo", "scheduler", "expirationDate", "shares",
    )
    require(taskArguments.keys.all { it in allowedFields }) {
        "Unknown task fields are not supported"
    }

    validateNullableWrapper(taskArguments, "parentTaskId") { value ->
        require(value == JsonNull || (value as? JsonPrimitive)?.intOrNull != null) {
            "task.parentTaskId.value must be an integer or null"
        }
    }
    validateNullableWrapper(taskArguments, "expirationDate") { value ->
        if (value != JsonNull) {
            val dateText = (value as? JsonPrimitive)?.takeIf { it.isString }?.content
                ?: throw IllegalArgumentException("task.expirationDate.value must be a string or null")
            require(runCatching { kotlinx.datetime.LocalDateTime.parse(dateText) }.isSuccess) {
                "task.expirationDate.value must use yyyy-MM-dd'T'HH:mm:ss"
            }
        }
    }
    validateNullableWrapper(taskArguments, "scheduler") { value ->
        if (value != JsonNull) validateSchedulerDto(value)
    }
    validateUpdateTaskShares(taskArguments["shares"])

    val update = McpJson.decodeFromJsonElement(UpdateTaskDto.serializer(), taskArguments)
    require(
        update.description != null ||
            update.parentTaskId != null ||
            update.priority != null ||
            update.isToDo != null ||
            update.scheduler != null ||
            update.expirationDate != null ||
            update.shares != null,
    ) { "At least one task field must be supplied for update" }
    return taskId to update
}


private fun validateNullableWrapper(
    arguments: JsonObject,
    fieldName: String,
    validateValue: (JsonElement) -> Unit,
) {
    val element = arguments[fieldName] ?: return
    if (element == JsonNull) return
    val wrapper = element as? JsonObject
        ?: throw IllegalArgumentException("$fieldName must be an object containing value, or null")
    require(wrapper.keys == setOf("value")) { "$fieldName must contain only the value field" }
    validateValue(wrapper["value"] ?: throw IllegalArgumentException("$fieldName.value is required"))
}

private fun validateSchedulerDto(element: JsonElement) {
    val scheduler = element as? JsonObject
        ?: throw IllegalArgumentException("scheduler.value must be an object or null")
    val allowedFields = setOf(
        "hour", "minute", "creationDate", "startDate", "lastDate",
        "daysOfWeek", "dayOfMonth", "repeatCount", "repeatInEveryPeriod", "type", "options",
    )
    require(scheduler.keys.all { it in allowedFields }) { "Unknown scheduler fields are not supported" }
}

private fun validateUpdateTaskShares(element: JsonElement?) {
    if (element == null || element == JsonNull) return
    val shares = element as? kotlinx.serialization.json.JsonArray
        ?: throw IllegalArgumentException("shares must be an array or null")
    shares.forEach { item ->
        val share = item as? JsonObject ?: throw IllegalArgumentException("Each share must be an object")
        require(share.keys == setOf("userId", "permission")) {
            "Each share must contain only userId and permission"
        }
        val userId = (share["userId"] as? JsonPrimitive)?.takeIf { it.isString }?.content
            ?: throw IllegalArgumentException("share.userId must be a string")
        require(userId.toIntOrNull() != null) { "share.userId must be a numeric user ID" }
        val permission = (share["permission"] as? JsonPrimitive)?.takeIf { it.isString }?.content
            ?: throw IllegalArgumentException("share.permission must be a string")
        require(permission.uppercase() in setOf("VIEWER", "EDITOR_AND_VIEWER")) {
            "share.permission is unsupported"
        }
    }
}
private fun decodeMarkAsToDoArguments(arguments: JsonObject): MarkAsToDoDto {
    require(arguments.keys == setOf("isToDo", "taskIds")) {
        "todo_mark_tasks expects only isToDo and taskIds"
    }
    val isToDoPrimitive = arguments["isToDo"] as? JsonPrimitive
    require(isToDoPrimitive != null && !isToDoPrimitive.isString && isToDoPrimitive.booleanOrNull != null) {
        "isToDo must be a boolean"
    }
    val isToDo = isToDoPrimitive.booleanOrNull!!
    val taskIds = arguments["taskIds"] as? JsonArray
        ?: throw IllegalArgumentException("taskIds must be an array")
    require(taskIds.isNotEmpty()) { "taskIds must not be empty" }
    val ids = taskIds.mapIndexed { index, element ->
        val primitive = element as? JsonPrimitive
        require(primitive != null && !primitive.isString && primitive.intOrNull != null) {
            "taskIds[$index] must be an integer"
        }
        val id = primitive.intOrNull!!
        require(id > 0) { "taskIds[$index] must be greater than zero" }
        id
    }
    return MarkAsToDoDto(isToDo = isToDo, taskIds = ids)
}


private fun decodeCopyTaskArguments(arguments: JsonObject): Int {
    require(arguments.keys == setOf("id")) {
        "todo_copy_task expects only an id"
    }
    val idPrimitive = arguments["id"] as? JsonPrimitive
    require(idPrimitive != null && !idPrimitive.isString && idPrimitive.intOrNull != null) {
        "id must be an integer"
    }
    val taskId = idPrimitive.intOrNull!!
    require(taskId > 0) { "id must be greater than zero" }
    return taskId
}

private fun decodeDeleteTaskArguments(arguments: JsonObject): Pair<Int, Boolean?> {
    require(arguments.keys.all { it in setOf("id", "removeWithSubtasks") }) {
        "todo_delete_task accepts only id and removeWithSubtasks"
    }
    val idPrimitive = arguments["id"] as? JsonPrimitive
    require(idPrimitive != null && !idPrimitive.isString && idPrimitive.intOrNull != null) {
        "id must be an integer"
    }
    val taskId = idPrimitive.intOrNull!!
    require(taskId > 0) { "id must be greater than zero" }

    val removeWithSubtasksPrimitive = arguments["removeWithSubtasks"] as? JsonPrimitive
    if ("removeWithSubtasks" in arguments) {
        require(
            removeWithSubtasksPrimitive != null &&
                !removeWithSubtasksPrimitive.isString &&
                removeWithSubtasksPrimitive.booleanOrNull != null,
        ) { "removeWithSubtasks must be a boolean" }
    }
    return taskId to removeWithSubtasksPrimitive?.booleanOrNull
}

private fun decodeLeaveShareArguments(arguments: JsonObject): LeaveShareDto {
    require(arguments.keys == setOf("id")) {
        "todo_leave_shared_task expects only an id"
    }
    val idPrimitive = arguments["id"] as? JsonPrimitive
    require(idPrimitive != null && !idPrimitive.isString && idPrimitive.intOrNull != null) {
        "id must be an integer"
    }
    val taskId = idPrimitive.intOrNull!!
    require(taskId > 0) { "id must be greater than zero" }
    return LeaveShareDto(id = taskId)
}

private fun decodeGetWeightArguments(arguments: JsonObject): Int {
    require(arguments.keys == setOf("id")) {
        "cheatday_get_weight expects only an id"
    }
    val idPrimitive = arguments["id"] as? JsonPrimitive
    require(idPrimitive != null && !idPrimitive.isString && idPrimitive.intOrNull != null) {
        "id must be an integer"
    }
    val weightId = idPrimitive.intOrNull!!
    require(weightId > 0) { "id must be greater than zero" }
    return weightId
}


private fun decodeCreateWeightArguments(arguments: JsonObject): PutWeightDto {
    require(arguments.keys == setOf("value", "date")) {
        "cheatday_create_weight expects only value and date"
    }
    val input = McpJson.decodeFromJsonElement(PutWeightDto.serializer(), arguments)
    require(input.value.isFinite()) { "value must be a finite number" }
    kotlinx.datetime.LocalDateTime.parse(input.date)
    return input
}

private fun typeUnion(vararg types: String) = buildJsonArray { types.forEach { add(JsonPrimitive(it)) } }

private fun <T> Server.addJsonTool(
    name: String,
    description: String,
    inputSchema: ToolSchema,
    serializer: KSerializer<T>,
    invalidArgumentsMessage: String,
    internalErrorMessage: String,
    execute: (JsonObject) -> HttpResponse<T>,
) {
    addTool(name = name, description = description, inputSchema = inputSchema) { request ->
        val arguments = request.params.arguments ?: buildJsonObject {}
        try {
            when (val response = execute(arguments)) {
                is HttpResponse.Success -> CallToolResult(
                    content = listOf(TextContent(text = McpJson.encodeToString(serializer, response.data))),
                )
                is HttpResponse.Error -> toolErrorResult(
                    if (response.httpStatusCode >= 500) internalErrorMessage else response.data.reason,
                )
            }
        } catch (exception: HttpException) {
            toolErrorResult(
                if (exception.httpStatus >= 500) internalErrorMessage
                else exception.message ?: invalidArgumentsMessage,
            )
        } catch (_: IllegalArgumentException) {
            toolErrorResult(invalidArgumentsMessage)
        } catch (_: Exception) {
            toolErrorResult(internalErrorMessage)
        }
    }
}


private fun toolErrorResult(message: String) = CallToolResult(content = listOf(TextContent(text = message)), isError = true)

private fun ApplicationCall.verifiedMcpPatOwner(
    personalAccessTokenService: () -> PersonalAccessTokenService,
): User? {
    val token = request.headers[HttpHeaders.Authorization].toBearerToken() ?: return null
    return try {
        personalAccessTokenService().verify(token)
    } catch (_: HttpException) {
        null
    }
}

private fun String?.toBearerToken(): String? {
    val value = this ?: return null
    val prefix = "Bearer "
    if (!value.startsWith(prefix, ignoreCase = true)) return null
    val token = value.substring(prefix.length)
    if (token.isBlank() || token.any(Char::isWhitespace)) return null
    return token
}
