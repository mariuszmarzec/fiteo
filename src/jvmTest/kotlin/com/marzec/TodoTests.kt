package com.marzec

import com.google.common.truth.Truth.assertThat
import com.marzec.core.CurrentTimeUtil
import com.marzec.fiteo.model.dto.ErrorDto
import com.marzec.fiteo.model.dto.NullableFieldDto
import com.marzec.todo.ApiPath
import com.marzec.todo.dto.TaskDto
import com.marzec.todo.model.MarkAsToDoDto
import com.marzec.todo.model.UpdateTaskDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext

class TodoTests {

    @Test
    fun getTasks() {
        testGetEndpoint(
            uri = ApiPath.TASKS,
            status = HttpStatusCode.OK,
            responseDto = listOf(
                taskDto.copy(id = 1),
                taskDto.copy(id = 2),
                taskDto.copy(id = 3)
            ),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto)
                addTask(createTaskDto)
                addTask(createTaskDto)
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
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
            },
            runRequestsAfter = {
                assertThat(getTasks()).isEqualTo(listOf(taskDto))
            }
        )
    }

    @Test
    fun addTask_scheduledOneShot() {
        testPostEndpoint(
            uri = ApiPath.ADD_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = createTaskDto.copy(scheduler = schedulerOneShotDto),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(scheduler = schedulerOneShotDto),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
            },
            runRequestsAfter = {
                assertThat(getTasks()).isEqualTo(listOf(taskDto.copy(scheduler = schedulerOneShotDto)))
            }
        )
    }

    @Test
    fun addTask_scheduledWithOptions() {
        val schedulerDto = schedulerOneShotDto.copy(
            options = mapOf(
                "highestPriorityAsDefault" to "true",
                "removeScheduled" to "true"
            )
        )
        testPostEndpoint(
            uri = ApiPath.ADD_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = createTaskDto.copy(scheduler = schedulerDto),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(scheduler = schedulerDto),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
            },
            runRequestsAfter = {
                assertThat(getTasks()).isEqualTo(
                    listOf(
                        taskDto.copy(scheduler = schedulerDto)
                    )
                )
            }
        )
    }

    @Test
    fun addTask_scheduledWeekly() {
        testPostEndpoint(
            uri = ApiPath.ADD_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = createTaskDto.copy(scheduler = schedulerWeeklyDto),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(scheduler = schedulerWeeklyDto),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
            },
            runRequestsAfter = {
                assertThat(getTasks()).isEqualTo(listOf(taskDto.copy(scheduler = schedulerWeeklyDto)))
            }
        )
    }

    @Test
    fun addTask_scheduledMonthly() {
        testPostEndpoint(
            uri = ApiPath.ADD_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = createTaskDto.copy(scheduler = schedulerMonthlyDto),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(scheduler = schedulerMonthlyDto),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
            },
            runRequestsAfter = {
                assertThat(getTasks()).isEqualTo(listOf(taskDto.copy(scheduler = schedulerMonthlyDto)))
            }
        )
    }

    @Test
    fun addTask_scheduledTaskCanNotHaveParent() {
        testPostEndpoint(
            uri = ApiPath.ADD_TASK,
            dto = createTaskDto.copy(parentTaskId = 1, scheduler = schedulerMonthlyDto),
            status = HttpStatusCode.BadRequest,
            responseDto = ErrorDto("Scheduled task can't have parent"),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto)
            },
            runRequestsAfter = {
                assertThat(getTasks()).isEqualTo(listOf(taskDto))
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
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto.copy(priority = -1))
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
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto)
                addTask(createTaskDto.copy(parentTaskId = 1, priority = -1))
            },
        )
    }

    @Test
    fun addSubTask_copyTask() {
        val taskToCopy = taskDto.copy(
            id = 2,
            parentTaskId = 1,
            priority = -1,
            subTasks = listOf(taskDto.copy(id = 3, parentTaskId = 2))
        )
        val copiedTask = taskDto.copy(
            id = 4,
            parentTaskId = 1,
            priority = -1,
            subTasks = listOf(taskDto.copy(id = 5, parentTaskId = 4)),
        )
        testGetEndpoint(
            uri = ApiPath.COPY_TASK.replace("{${Api.Args.ARG_ID}}", "2"),
            status = HttpStatusCode.OK,
            responseDto = copiedTask,
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto)
                addTask(createTaskDto.copy(parentTaskId = 1, priority = -1))
                addTask(createTaskDto.copy(parentTaskId = 2))
            },
            runRequestsAfter = {
                assertThat(getTasks()).isEqualTo(
                    listOf(taskDto.copy(subTasks = listOf(taskToCopy, copiedTask)))
                )
            }
        )
    }

    @Test
    fun addTask_withHighestPriorityByDefault() {
        testPostEndpoint(
            uri = ApiPath.ADD_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = createTaskDto.copy(priority = null, highestPriorityAsDefault = true),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(id = 2, priority = 0),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto.copy(priority = -1))
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
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto)
                addTask(createTaskDto.copy(parentTaskId = 1, priority = -1))
            },
        )
    }

    @Test
    fun addSubTask_withoutHighestPriority_withParentTaskPriority() {
        testPostEndpoint(
            uri = ApiPath.ADD_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = createTaskDto.copy(parentTaskId = 1, priority = null, highestPriorityAsDefault = false),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(id = 2, parentTaskId = 1, priority = -100),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto.copy(priority = -100))
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
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto)
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
    fun markTaskAsToDo_asToDo() {
        testPostEndpoint(
            uri = ApiPath.MARK_AS_TO_DO,
            dto = MarkAsToDoDto(
                isToDo = true,
                taskIds = listOf(2, 3)
            ),
            status = HttpStatusCode.OK,
            responseDto = listOf(
                taskDto.copy(id = 2, isToDo = true),
                taskDto.copy(id = 3, isToDo = true),
                taskDto.copy(id = 1, isToDo = false)
            ),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto)
                addTask(createTaskDto)
                addTask(createTaskDto)
                updateTask("1", UpdateTaskDto(description = "task", isToDo = false))
                updateTask("2", UpdateTaskDto(description = "task", isToDo = false))
                updateTask("3", UpdateTaskDto(description = "task", isToDo = false))
            },
        )
    }

    @Test
    fun markTaskAsToDo_asDone() {
        testPostEndpoint(
            uri = ApiPath.MARK_AS_TO_DO,
            dto = MarkAsToDoDto(
                isToDo = false,
                taskIds = listOf(2, 3)
            ),
            status = HttpStatusCode.OK,
            responseDto = listOf(
                taskDto.copy(id = 1),
                taskDto.copy(id = 2, isToDo = false),
                taskDto.copy(id = 3, isToDo = false)
            ),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto)
                addTask(createTaskDto)
                addTask(createTaskDto)
            }
        )
    }

    @Test
    fun removeTaskWithSubtasks() {
        testDeleteEndpoint(
            uri = ApiPath.DELETE_TASK.replace("{${Api.Args.ARG_ID}}", "1") + "?removeWithSubtasks=true",
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(
                subTasks = listOf(
                    taskDto.copy(id = 2, parentTaskId = 1),
                    taskDto.copy(id = 3, parentTaskId = 1)
                )
            ),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto)
                addTask(createTaskDto.copy(parentTaskId = 1))
                addTask(createTaskDto.copy(parentTaskId = 1))
            },
            runRequestsAfter = {
                assertThat(getTasks()).isEqualTo(emptyList<TaskDto>())
            }
        )
    }

    @Test
    fun removeTaskWithSubtasks_removeSubtasksFalse() {
        testDeleteEndpoint(
            uri = ApiPath.DELETE_TASK.replace("{${Api.Args.ARG_ID}}", "1") + "?removeWithSubtasks=false",
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(
                subTasks = listOf(
                    taskDto.copy(id = 2, parentTaskId = 1),
                    taskDto.copy(id = 3, parentTaskId = 1)
                )
            ),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto)
                addTask(createTaskDto.copy(parentTaskId = 1))
                addTask(createTaskDto.copy(parentTaskId = 1))
            },
            runRequestsAfter = {
                assertThat(getTasks()).isEqualTo(
                    listOf(
                        taskDto.copy(id = 2),
                        taskDto.copy(id = 3)
                    )
                )
            }
        )
    }

    @Test
    fun removeTaskWithSubtasks_removeChildWithoutRemovingSubtasks() {
        testDeleteEndpoint(
            uri = ApiPath.DELETE_TASK.replace("{${Api.Args.ARG_ID}}", "2") + "?removeWithSubtasks=false",
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(
                id = 2,
                parentTaskId = 1,
                subTasks = listOf(
                    taskDto.copy(id = 3, parentTaskId = 2),
                    taskDto.copy(id = 4, parentTaskId = 2, isToDo = false)
                )
            ),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto)
                addTask(createTaskDto.copy(parentTaskId = 1))
                addTask(createTaskDto.copy(parentTaskId = 2))
                addTask(createTaskDto.copy(parentTaskId = 2, isToDo = false))
            },
            runRequestsAfter = {
                assertThat(getTasks()).isEqualTo(
                    listOf(
                        taskDto.copy(
                            id = 1,
                            subTasks = listOf(
                                taskDto.copy(id = 3, parentTaskId = 1),
                                taskDto.copy(id = 4, parentTaskId = 1, isToDo = false)
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
            dto = UpdateTaskDto(
                description = "updated task",
                parentTaskId = NullableFieldDto(null),
                priority = 10,
                isToDo = false,
                scheduler = NullableFieldDto(schedulerWeeklyDto)
            ),
            headers = mapOf("Version" to "V2"),
            status = HttpStatusCode.OK,
            responseDto = stubTaskDto(
                description = "updated task",
                priority = 10,
                isToDo = false,
                scheduler = schedulerWeeklyDto
            ),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto)
            },
            runRequestsAfter = {
                assertThat(getTasks()).isEqualTo(
                    listOf(
                        taskDto.copy(
                            description = "updated task",
                            parentTaskId = null,
                            priority = 10,
                            isToDo = false,
                            scheduler = schedulerWeeklyDto
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
            dto = UpdateTaskDto(
                description=  "task2",
                parentTaskId = NullableFieldDto(1)
            ),
            headers = mapOf("Version" to "V2"),
            status = HttpStatusCode.OK,
            responseDto = stubTaskDto(id = 2, description = "task2", parentTaskId = 1),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(stubCreateTaskDto("task", null, 0))
                addTask(stubCreateTaskDto("task2", null, 0))
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
            dto = UpdateTaskDto(
                description = "task2",
                parentTaskId = NullableFieldDto(null)
            ),
            headers = mapOf("Version" to "V2"),
            status = HttpStatusCode.OK,
            responseDto = stubTaskDto(id = 2, description = "task2", parentTaskId = null),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(stubCreateTaskDto("task", null, 0))
                addTask(stubCreateTaskDto("task2", 1, 0))
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
    fun updateTask_scheduledTaskCanNotHaveParent() {
        testPatchEndpoint(
            uri = ApiPath.UPDATE_TASK.replace("{${Api.Args.ARG_ID}}", "2"),
            dto = UpdateTaskDto(
                parentTaskId = NullableFieldDto(1),
                scheduler = NullableFieldDto(schedulerMonthlyDto)
            ),
            headers = mapOf("Version" to "V2"),
            status = HttpStatusCode.BadRequest,
            responseDto = ErrorDto("Scheduled task can't have parent"),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto)
                addTask(createTaskDto)
            },
            runRequestsAfter = {
                assertThat(getTasks()).isEqualTo(listOf(taskDto, taskDto.copy(id = 2)))
            }
        )
    }

    @Test
    fun removeTask() {
        testDeleteEndpoint(
            uri = ApiPath.DELETE_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            status = HttpStatusCode.OK,
            responseDto = taskDto,
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto)
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
            uri = ApiPath.TASKS,
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
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto.copy(priority = 1))
                addTask(createTaskDto.copy(priority = 2))

                CurrentTimeUtil.setOtherTime(17, 5, 2021)
                addTask(createTaskDto.copy(priority = 2))

                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto.copy(priority = 1, parentTaskId = 1))
                addTask(createTaskDto.copy(priority = 2, parentTaskId = 1))

                CurrentTimeUtil.setOtherTime(18, 5, 2021)
                addTask(createTaskDto.copy(priority = 2, parentTaskId = 1))
            }
        )
    }

    @Test
    fun todoLists_getSortedListWithDoneTasks() {
        testGetEndpoint(
            uri = ApiPath.TASKS,
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
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto.copy(priority = 1))
                addTask(createTaskDto.copy(priority = 2))

                CurrentTimeUtil.setOtherTime(17, 5, 2021)
                addTask(createTaskDto.copy(priority = 2))

                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto.copy(priority = 1, parentTaskId = 1))
                addTask(createTaskDto.copy(priority = 2, parentTaskId = 1))

                CurrentTimeUtil.setOtherTime(18, 5, 2021)
                addTask(createTaskDto.copy(priority = 2, parentTaskId = 1))

                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto.copy(priority = 20))

                markAsDone(1)
                markAsDone(2)
                CurrentTimeUtil.setOtherTime(17, 5, 2021)
                markAsDone(3)
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                markAsDone(4)
                markAsDone(5)
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
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto)
                addTask(createTaskDto.copy(parentTaskId = 1))
                addTask(createTaskDto.copy(parentTaskId = 2))
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
