package com.marzec

import com.google.common.truth.Truth.assertThat
import com.marzec.core.CurrentTimeUtil
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
            uri = ApiPath.DELETE_TODO_LIST.replace("{${Api.Args.ARG_ID}}", "1"),
            status = HttpStatusCode.OK,
            responseDto = todoListDto,
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                addTodoList(createTodoListDto)
                addTodoList(createTodoListDto2)
            },
            runRequestsAfter = {
                assertThat(getTodoLists()).isEqualTo(
                    listOf(
                        todoListDto2
                    )
                )
            }
        )
    }

    @Test
    fun addTask() {
        testPostEndpoint(
            uri = ApiPath.ADD_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = createTaskDto,
            status = HttpStatusCode.OK,
            responseDto = taskDto,
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTodoList(createTodoListDto)
            },
            runRequestsAfter = {
                assertThat(getTodoLists()).isEqualTo(
                    listOf(
                        todoListDto.copy(tasks = listOf(taskDto))
                    )
                )
            }
        )
    }

    @Test
    fun addTask_withLowestPriorityByDefault() {
        testPostEndpoint(
            uri = ApiPath.ADD_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = createTaskDto.copy(priority = null),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(id = 2, priority = -2),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTodoList(createTodoListDto)
                addTask(1, createTaskDto.copy(priority = -1))
            },
        )
    }

    @Test
    fun addSubTask_withLowestPriorityByDefault() {
        testPostEndpoint(
            uri = ApiPath.ADD_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = createTaskDto.copy(parentTaskId = 1, priority = null),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(id = 3, parentTaskId = 1, priority = -2),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTodoList(createTodoListDto)
                addTask(1, createTaskDto)
                addTask(1, createTaskDto.copy(parentTaskId = 1, priority = -1))
            },
        )
    }

    @Test
    fun addTask_withHighestPriorityByDefault() {
        testPostEndpoint(
            uri = ApiPath.ADD_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = createTaskDto.copy(priority = null, highestPriorityAsDefault = true),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(id = 2, priority = 0),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTodoList(createTodoListDto)
                addTask(1, createTaskDto.copy(priority = -1))
            },
        )
    }

    @Test
    fun addSubTask_withHighestPriorityByDefault() {
        testPostEndpoint(
            uri = ApiPath.ADD_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = createTaskDto.copy(parentTaskId = 1, priority = null, highestPriorityAsDefault = true),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(id = 3, parentTaskId = 1, priority = 0),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTodoList(createTodoListDto)
                addTask(1, createTaskDto)
                addTask(1, createTaskDto.copy(parentTaskId = 1, priority = -1))
            },
        )
    }

    @Test
    fun addTask_asChildTask() {
        testPostEndpoint(
            uri = ApiPath.ADD_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = stubCreateTaskDto(description = "subtask", parentTaskId = 1),
            status = HttpStatusCode.OK,
            responseDto = stubTaskDto(id = 2, description = "subtask", parentTaskId = 1),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTodoList(createTodoListDto)
                addTask(1, createTaskDto)
            },
            runRequestsAfter = {
                assertThat(getTodoLists()).isEqualTo(
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
            uri = ApiPath.UPDATE_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = UpdateTaskDto(description = "updated task", parentTaskId = null, priority = 10, isToDo = false),
            status = HttpStatusCode.OK,
            responseDto = stubTaskDto(description = "updated task", priority = 10, isToDo = false),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTodoList(createTodoListDto)
                addTask(1, createTaskDto)
            },
            runRequestsAfter = {
                assertThat(getTodoLists()).isEqualTo(
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
            uri = ApiPath.UPDATE_TASK.replace("{${Api.Args.ARG_ID}}", "2"),
            dto = stubUpdateTaskDto(description = "task2", parentTaskId = 1),
            status = HttpStatusCode.OK,
            responseDto = stubTaskDto(id = 2, description = "task2", parentTaskId = 1),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTodoList(createTodoListDto)
                addTask(1, stubCreateTaskDto("task", null, 0))
                addTask(1, stubCreateTaskDto("task2", null, 0))
                assertThat(getTodoLists()).isEqualTo(
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
                assertThat(getTodoLists()).isEqualTo(
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
            uri = ApiPath.UPDATE_TASK.replace("{${Api.Args.ARG_ID}}", "2"),
            dto = stubUpdateTaskDto(description = "task2", parentTaskId = null),
            status = HttpStatusCode.OK,
            responseDto = stubTaskDto(id = 2, description = "task2", parentTaskId = null),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTodoList(createTodoListDto)
                addTask(1, stubCreateTaskDto("task", null, 0))
                addTask(1, stubCreateTaskDto("task2", 1, 0))
                assertThat(getTodoLists()).isEqualTo(
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
                assertThat(getTodoLists()).isEqualTo(
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
            uri = ApiPath.DELETE_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            status = HttpStatusCode.OK,
            responseDto = taskDto,
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTodoList(createTodoListDto)
                addTask(1, createTaskDto)
                assertThat(getTodoLists()).isEqualTo(
                    listOf(
                        todoListDto.copy(tasks = listOf(taskDto))
                    )
                )
            },
            runRequestsAfter = {
                assertThat(getTodoLists()).isEqualTo(listOf(todoListDto))
            }
        )
    }

    @Test
    fun todoLists_getSortedList() {
        testGetEndpoint(
            uri = ApiPath.TODO_LISTS,
            status = HttpStatusCode.OK,
            responseDto = listOf(
                todoListDto.copy(
                    tasks = listOf(
                        taskDto.copy(id = 2, priority = 2),
                        taskDto.copy(
                            id = 3,
                            priority = 2,
                            addedTime = "2021-05-17T00:00:00",
                            modifiedTime = "2021-05-17T00:00:00",
                        ),
                        taskDto.copy(
                            id = 1, priority = 1,
                            subTasks = listOf(
                                taskDto.copy(id = 5, priority = 2, parentTaskId = 1),
                                taskDto.copy(
                                    id = 6,
                                    priority = 2,
                                    parentTaskId = 1,
                                    addedTime = "2021-05-18T00:00:00",
                                    modifiedTime = "2021-05-18T00:00:00",
                                ),
                                taskDto.copy(id = 4, priority = 1, parentTaskId = 1)
                            )
                        )
                    )
                )
            ),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTodoList(createTodoListDto)
                addTask(1, createTaskDto.copy(priority = 1))
                addTask(1, createTaskDto.copy(priority = 2))

                CurrentTimeUtil.setOtherTime(17, 5, 2021)
                addTask(1, createTaskDto.copy(priority = 2))

                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(1, createTaskDto.copy(priority = 1, parentTaskId = 1))
                addTask(1, createTaskDto.copy(priority = 2, parentTaskId = 1))

                CurrentTimeUtil.setOtherTime(18, 5, 2021)
                addTask(1, createTaskDto.copy(priority = 2, parentTaskId = 1))
            }
        )
    }

    @Test
    fun removeTask_pinSubtaskToParentOfParent() {
        val subtask = taskDto.copy(
            id = 3,
            parentTaskId = 2
        )
        val removedTask = taskDto.copy(
            id = 2,
            parentTaskId = 1,
            subTasks = listOf(subtask)
        )
        testDeleteEndpoint(
            uri = ApiPath.DELETE_TASK.replace("{${Api.Args.ARG_ID}}", "2"),
            status = HttpStatusCode.OK,
            responseDto = removedTask,
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTodoList(createTodoListDto)
                addTask(1, createTaskDto)
                addTask(1, createTaskDto.copy(parentTaskId = 1))
                addTask(1, createTaskDto.copy(parentTaskId = 2))
                assertThat(getTodoLists()).isEqualTo(
                    listOf(
                        todoListDto.copy(
                            tasks = listOf(
                                taskDto.copy(subTasks = listOf(removedTask))
                            )
                        )
                    )
                )
            },
            runRequestsAfter = {
                assertThat(getTodoLists()).isEqualTo(
                    listOf(
                        todoListDto.copy(
                            tasks = listOf(
                                taskDto.copy(subTasks = listOf(subtask.copy(parentTaskId = 1)))
                            )
                        )
                    )
                )
            }
        )
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }
}

