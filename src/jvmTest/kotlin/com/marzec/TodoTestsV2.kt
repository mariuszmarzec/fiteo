package com.marzec

import com.google.common.truth.Truth.assertThat
import com.marzec.core.CurrentTimeUtil
import com.marzec.todo.ApiPath
import com.marzec.todo.dto.TaskDto
import com.marzec.todo.model.UpdateTaskDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext

class TodoTestsV2 {

    @Test
    fun getTasks() {
        testGetEndpoint(
            uri = ApiPath.V2_TASKS,
            status = HttpStatusCode.OK,
            responseDto = listOf(
                taskDto.copy(id = 1),
                taskDto.copy(id = 2),
                taskDto.copy(id = 3)
            ),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTodoList(createTodoListDto)
                addTaskV2(createTaskDto)
                addTaskV2(createTaskDto)
                addTaskV2(createTaskDto)
            }
        )
    }

    @Test
    fun addTask() {
        testPostEndpoint(
            uri = ApiPath.V2_ADD_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = createTaskDto,
            status = HttpStatusCode.OK,
            responseDto = taskDto,
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTodoList(createTodoListDto)
            },
            runRequestsAfter = {
                assertThat(getTasks()).isEqualTo(listOf(taskDto))
            }
        )
    }

    @Test
    fun addTask_withLowestPriorityByDefault() {
        testPostEndpoint(
            uri = ApiPath.V2_ADD_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = createTaskDto.copy(priority = null),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(id = 2, priority = -2),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTodoList(createTodoListDto)
                addTaskV2(createTaskDto.copy(priority = -1))
            },
        )
    }

    @Test
    fun addSubTask_withLowestPriorityByDefault() {
        testPostEndpoint(
            uri = ApiPath.V2_ADD_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = createTaskDto.copy(parentTaskId = 1, priority = null),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(id = 3, parentTaskId = 1, priority = -2),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTodoList(createTodoListDto)
                addTaskV2(createTaskDto)
                addTaskV2(createTaskDto.copy(parentTaskId = 1, priority = -1))
            },
        )
    }

    @Test
    fun addTask_withHighestPriorityByDefault() {
        testPostEndpoint(
            uri = ApiPath.V2_ADD_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = createTaskDto.copy(priority = null, highestPriorityAsDefault = true),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(id = 2, priority = 0),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTodoList(createTodoListDto)
                addTaskV2(createTaskDto.copy(priority = -1))
            },
        )
    }

    @Test
    fun addSubTask_withHighestPriorityByDefault() {
        testPostEndpoint(
            uri = ApiPath.V2_ADD_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = createTaskDto.copy(parentTaskId = 1, priority = null, highestPriorityAsDefault = true),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(id = 3, parentTaskId = 1, priority = 0),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTodoList(createTodoListDto)
                addTaskV2(createTaskDto)
                addTaskV2(createTaskDto.copy(parentTaskId = 1, priority = -1))
            },
        )
    }

    @Test
    fun addTask_asChildTask() {
        testPostEndpoint(
            uri = ApiPath.V2_ADD_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = stubCreateTaskDto(description = "subtask", parentTaskId = 1),
            status = HttpStatusCode.OK,
            responseDto = stubTaskDto(id = 2, description = "subtask", parentTaskId = 1),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTodoList(createTodoListDto)
                addTaskV2(createTaskDto)
            },
            runRequestsAfter = {
                assertThat(getTasks()).isEqualTo(
                    listOf(
                        taskDto.copy(
                            subTasks = listOf(
                                stubTaskDto(id = 2, description = "subtask", parentTaskId = 1)
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
                addTaskV2(createTaskDto)
            },
            runRequestsAfter = {
                assertThat(getTasks()).isEqualTo(
                    listOf(
                        taskDto.copy(
                            description = "updated task",
                            parentTaskId = null,
                            priority = 10,
                            isToDo = false
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
                addTaskV2(stubCreateTaskDto("task", null, 0))
                addTaskV2(stubCreateTaskDto("task2", null, 0))
                assertThat(getTasks()).isEqualTo(
                    listOf(
                        stubTaskDto(id = 1, description = "task"),
                        stubTaskDto(id = 2, description = "task2")
                    )
                )
            },
            runRequestsAfter = {
                assertThat(getTasks()).isEqualTo(
                    listOf(
                        stubTaskDto(
                            id = 1,
                            description = "task",
                            subTasks = listOf(
                                stubTaskDto(id = 2, description = "task2", parentTaskId = 1)
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
                addTaskV2(stubCreateTaskDto("task", null, 0))
                addTaskV2(stubCreateTaskDto("task2", 1, 0))
                assertThat(getTasks()).isEqualTo(
                    listOf(
                        stubTaskDto(
                            id = 1,
                            description = "task",
                            subTasks = listOf(
                                stubTaskDto(id = 2, description = "task2", parentTaskId = 1)
                            )
                        )
                    )
                )
            },
            runRequestsAfter = {
                assertThat(getTasks()).isEqualTo(
                    listOf(
                        stubTaskDto(id = 1, description = "task"),
                        stubTaskDto(id = 2, description = "task2")
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
                addTaskV2(createTaskDto)
                assertThat(getTasks()).isEqualTo(
                    listOf(taskDto)
                )
            },
            runRequestsAfter = {
                assertThat(getTasks()).isEqualTo(listOf<TaskDto>())
            }
        )
    }

    @Test
    fun todoLists_getSortedList() {
        testGetEndpoint(
            uri = ApiPath.V2_TASKS,
            status = HttpStatusCode.OK,
            responseDto = listOf(
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
            ),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTodoList(createTodoListDto)
                addTaskV2(createTaskDto.copy(priority = 1))
                addTaskV2(createTaskDto.copy(priority = 2))

                CurrentTimeUtil.setOtherTime(17, 5, 2021)
                addTaskV2(createTaskDto.copy(priority = 2))

                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTaskV2(createTaskDto.copy(priority = 1, parentTaskId = 1))
                addTaskV2(createTaskDto.copy(priority = 2, parentTaskId = 1))

                CurrentTimeUtil.setOtherTime(18, 5, 2021)
                addTaskV2(createTaskDto.copy(priority = 2, parentTaskId = 1))
            }
        )
    }

    @Test
    fun todoLists_getSortedListWithDoneTasks() {
        testGetEndpoint(
            uri = ApiPath.V2_TASKS,
            status = HttpStatusCode.OK,
            responseDto = listOf(
                taskDto.copy(id = 7, priority = 20),
                taskDto.copy(id = 2, priority = 2, isToDo = false),
                taskDto.copy(
                    id = 3,
                    priority = 2,
                    addedTime = "2021-05-17T00:00:00",
                    modifiedTime = "2021-05-17T00:00:00",
                    isToDo = false
                ),
                taskDto.copy(
                    id = 1,
                    priority = 1,
                    isToDo = false,
                    subTasks = listOf(
                        taskDto.copy(
                            id = 6,
                            priority = 2,
                            parentTaskId = 1,
                            addedTime = "2021-05-18T00:00:00",
                            modifiedTime = "2021-05-18T00:00:00"
                        ),
                        taskDto.copy(id = 5, priority = 2, parentTaskId = 1, isToDo = false),
                        taskDto.copy(id = 4, priority = 1, parentTaskId = 1, isToDo = false)
                    )
                )
            ),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTodoList(createTodoListDto)
                addTaskV2(createTaskDto.copy(priority = 1))
                addTaskV2(createTaskDto.copy(priority = 2))

                CurrentTimeUtil.setOtherTime(17, 5, 2021)
                addTaskV2(createTaskDto.copy(priority = 2))

                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTaskV2(createTaskDto.copy(priority = 1, parentTaskId = 1))
                addTaskV2(createTaskDto.copy(priority = 2, parentTaskId = 1))

                CurrentTimeUtil.setOtherTime(18, 5, 2021)
                addTaskV2(createTaskDto.copy(priority = 2, parentTaskId = 1))

                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTaskV2(createTaskDto.copy(priority = 20))

                markAsDone(1, 1)
                markAsDone(1, 2)
                CurrentTimeUtil.setOtherTime(17, 5, 2021)
                markAsDone(1, 3)
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                markAsDone(1, 4)
                markAsDone(1, 5)
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
                addTaskV2(createTaskDto)
                addTaskV2(createTaskDto.copy(parentTaskId = 1))
                addTaskV2(createTaskDto.copy(parentTaskId = 2))
                assertThat(getTasks()).isEqualTo(
                    listOf(
                        taskDto.copy(subTasks = listOf(removedTask))
                    )
                )
            },
            runRequestsAfter = {
                assertThat(getTasks()).isEqualTo(
                    listOf(
                        taskDto.copy(subTasks = listOf(subtask.copy(parentTaskId = 1)))
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

