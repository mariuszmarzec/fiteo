package com.marzec

import com.google.common.truth.Truth
import com.marzec.exercises.createTaskDto
import com.marzec.exercises.createTodoListDto
import com.marzec.exercises.createTodoListDto2
import com.marzec.exercises.stubCreateTaskDto
import com.marzec.exercises.stubTaskDto
import com.marzec.exercises.stubUpdateTaskDto
import com.marzec.exercises.taskDto
import com.marzec.exercises.todoListDto
import com.marzec.exercises.todoListDto2
import com.marzec.todo.ApiPath
import com.marzec.todo.model.UpdateTaskDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext

class TodoTests {

    @Test
    fun addTodoList() {
        testPostEndpoint(
            uri = ApiPath.TODO_LIST,
            dto = createTodoListDto,
            status = HttpStatusCode.OK,
            responseDto = todoListDto,
            authorize = TestApplicationEngine::registerAndLogin
        )
    }

    @Test
    fun todoLists() {
        testGetEndpoint(
            uri = ApiPath.TODO_LISTS,
            status = HttpStatusCode.OK,
            responseDto = listOf(
                todoListDto,
                todoListDto2
            ),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                addTodoList(createTodoListDto)
                addTodoList(createTodoListDto2)
            }
        )
    }

    @Test
    fun deleteTodoList() {
        testDeleteEndpoint(
            uri = ApiPath.TODO_LISTS.replace("{id}", "1"),
            status = HttpStatusCode.OK,
            responseDto = todoListDto,
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                addTodoList(createTodoListDto)
                addTodoList(createTodoListDto2)
            },
            runRequestsAfter = {
                Truth.assertThat(getTodoLists()).isEqualTo(
                    listOf(
                        createTodoListDto2
                    )
                )
            }
        )
    }

    @Test
    fun addTask() {
        testPostEndpoint(
            uri = ApiPath.ADD_TASK.replace("{${ApiPath.ARG_ID}}", "1"),
            dto = createTaskDto,
            status = HttpStatusCode.OK,
            responseDto = taskDto,
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                addTodoList(createTodoListDto)
            },
            runRequestsAfter = {
                Truth.assertThat(getTodoLists()).isEqualTo(
                    listOf(
                        todoListDto.copy(tasks = listOf(taskDto))
                    )
                )
            }
        )
    }

    @Test
    fun addTask_asChildTask() {
        testPostEndpoint(
            uri = ApiPath.ADD_TASK.replace("{${ApiPath.ARG_ID}}", "1"),
            dto = stubCreateTaskDto(description = "subtask", parentTaskId = 1),
            status = HttpStatusCode.OK,
            responseDto = stubTaskDto(id = 2, description = "subtask", parentTaskId = 1),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                addTodoList(createTodoListDto)
                addTask(1, createTaskDto)
            },
            runRequestsAfter = {
                Truth.assertThat(getTodoLists()).isEqualTo(
                    listOf(
                        todoListDto.copy(
                            tasks = listOf(
                                taskDto.copy(
                                    subTasks = listOf(
                                        stubTaskDto(id = 2, description = "subtask", parentTaskId = 1)
                                    )
                                )
                            )
                        )
                    )
                )
            }
        )
    }

    @Test
    fun updateTask() {
        testPatchEndpoint(
            uri = ApiPath.UPDATE_TASK.replace("{${ApiPath.ARG_ID}}", "1"),
            dto = UpdateTaskDto(description = "updated task", parentTaskId = null, priority = 10, isToDo = false),
            status = HttpStatusCode.OK,
            responseDto = stubTaskDto(description = "updated task", priority = 10, isToDo = false),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                addTodoList(createTodoListDto)
                addTask(1, createTaskDto)
            },
            runRequestsAfter = {
                Truth.assertThat(getTodoLists()).isEqualTo(
                    listOf(
                        todoListDto.copy(
                            tasks = listOf(
                                taskDto.copy(
                                    description = "updated task",
                                    parentTaskId = null,
                                    priority = 10,
                                    isToDo = false
                                )
                            )
                        )
                    )
                )
            }
        )
    }

    @Test
    fun updateTask_pinToParentTask() {
        testPatchEndpoint(
            uri = ApiPath.UPDATE_TASK.replace("{${ApiPath.ARG_ID}}", "1"),
            dto = stubUpdateTaskDto(description = "task2", parentTaskId = 1),
            status = HttpStatusCode.OK,
            responseDto = stubTaskDto(description = "task2", parentTaskId = 1),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                addTodoList(createTodoListDto)
                addTask(1, stubCreateTaskDto("task", null, 0))
                addTask(1, stubCreateTaskDto("task2", null, 0))
                Truth.assertThat(getTodoLists()).isEqualTo(
                    listOf(
                        todoListDto.copy(
                            tasks = listOf(
                                stubTaskDto(id = 1, description = "task"),
                                stubTaskDto(id = 2, description = "task2")
                            )
                        )
                    )
                )
            },
            runRequestsAfter = {
                Truth.assertThat(getTodoLists()).isEqualTo(
                    listOf(
                        todoListDto.copy(
                            tasks = listOf(
                                stubTaskDto(
                                    id = 1,
                                    description = "task",
                                    subTasks = listOf(
                                        stubTaskDto(id = 2, description = "task2", parentTaskId = 1)
                                    )
                                )
                            )
                        )
                    )
                )
            }
        )
    }

    @Test
    fun updateTask_unpinFromParentTask() {
        testPatchEndpoint(
            uri = ApiPath.UPDATE_TASK.replace("{${ApiPath.ARG_ID}}", "1"),
            dto = stubUpdateTaskDto(description = "task2", parentTaskId = null),
            status = HttpStatusCode.OK,
            responseDto = stubTaskDto(description = "task2", parentTaskId = null),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                addTodoList(createTodoListDto)
                addTask(1, stubCreateTaskDto("task", null, 0))
                addTask(1, stubCreateTaskDto("task2", 1, 0))
                Truth.assertThat(getTodoLists()).isEqualTo(
                    listOf(
                        todoListDto.copy(
                            tasks = listOf(
                                stubTaskDto(
                                    id = 1,
                                    description = "task",
                                    subTasks = listOf(
                                        stubTaskDto(id = 2, description = "task2", parentTaskId = 1)
                                    )
                                )
                            )
                        )
                    )
                )
            },
            runRequestsAfter = {
                Truth.assertThat(getTodoLists()).isEqualTo(
                    listOf(
                        todoListDto.copy(
                            tasks = listOf(
                                stubTaskDto(id = 1, description = "task"),
                                stubTaskDto(id = 2, description = "task2")
                            )
                        )
                    )
                )
            }
        )
    }

    @Test
    fun removeTask() {
        testDeleteEndpoint(
            uri = ApiPath.DELETE_TASK.replace("{${ApiPath.ARG_ID}}", "1"),
            status = HttpStatusCode.OK,
            responseDto = taskDto,
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                addTodoList(createTodoListDto)
                addTask(1, createTaskDto)
                Truth.assertThat(getTodoLists()).isEqualTo(
                    listOf(
                        todoListDto.copy(tasks = listOf(taskDto))
                    )
                )
            },
            runRequestsAfter = {
                Truth.assertThat(getTodoLists()).isEqualTo(listOf(todoListDto))
            }
        )
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }
}