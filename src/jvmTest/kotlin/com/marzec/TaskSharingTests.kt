package com.marzec

import com.google.common.truth.Truth.assertThat
import com.marzec.core.CurrentTimeUtil
import com.marzec.fiteo.model.dto.LoginRequestDto
import com.marzec.todo.ApiPath
import com.marzec.todo.model.TaskShareDto
import com.marzec.todo.model.UpdateTaskDto
import com.marzec.todo.model.UpdateTaskShareDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext

class TaskSharingTests {

    private val user1Register = stubRegisterRequestDto("user1@mail.com", "password", "password")
    private val user1Login = LoginRequestDto("user1@mail.com", "password")
    
    private val user2Register = stubRegisterRequestDto("user2@mail.com", "password", "password")
    private val user2Login = LoginRequestDto("user2@mail.com", "password")

    @Test
    fun shareTaskOnCreation() {
        testPostEndpoint(
            uri = ApiPath.ADD_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = createTaskDto.copy(
                shares = listOf(TaskShareDto("2", "VIEWER"))
            ),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(shares = listOf(TaskShareDto("2", "VIEWER"))),
            authorize = {
                register(user2Register)
                registerAndLogin(user1Login)
            },
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
            },
            runRequestsAfter = {
                val tasksUser1 = getTasks()
                assertThat(tasksUser1.first().shares).isEqualTo(listOf(TaskShareDto("2", "VIEWER")))
                
                authToken = login(user2Login)
                val tasksUser2 = getTasks()
                assertThat(tasksUser2.first().shares).isEqualTo(listOf(TaskShareDto("2", "VIEWER")))
                assertThat(tasksUser2.first().description).isEqualTo("task")
            }
        )
    }

    @Test
    fun updateTask_ownerModifiesShares() {
        testPatchEndpoint(
            uri = ApiPath.UPDATE_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = UpdateTaskDto(
                shares = listOf(UpdateTaskShareDto("2", "EDITOR_AND_VIEWER", false))
            ),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(shares = listOf(TaskShareDto("2", "EDITOR_AND_VIEWER"))),
            authorize = {
                register(user2Register)
                registerAndLogin(user1Login)
            },
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto.copy(shares = listOf(TaskShareDto("2", "VIEWER"))))
            },
            runRequestsAfter = {
                val tasksUser1 = getTasks()
                assertThat(tasksUser1.first().shares).isEqualTo(listOf(TaskShareDto("2", "EDITOR_AND_VIEWER")))
            }
        )
    }
    
    @Test
    fun updateTask_userUnshares() {
        testPatchEndpoint(
            uri = ApiPath.UPDATE_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = UpdateTaskDto(
                shares = listOf(UpdateTaskShareDto("2", "VIEWER", true))
            ),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(shares = emptyList()),
            authorize = {
                register(user2Register)
                registerAndLogin(user1Login)
            },
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto.copy(shares = listOf(TaskShareDto("2", "VIEWER"))))
                authToken = login(user2Login)
            },
            runRequestsAfter = {
                val tasksUser2 = getTasks()
                assertThat(tasksUser2).isEmpty()
                
                authToken = login(user1Login)
                val tasksUser1 = getTasks()
                assertThat(tasksUser1.first().shares).isEmpty()
            }
        )
    }

    @Test
    fun updateTask_editorUpdatesTask() {
        testPatchEndpoint(
            uri = ApiPath.UPDATE_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = UpdateTaskDto(
                description = "updated by editor"
            ),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(description = "updated by editor", shares = listOf(TaskShareDto("2", "EDITOR_AND_VIEWER"))),
            authorize = {
                register(user2Register)
                registerAndLogin(user1Login)
            },
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto.copy(shares = listOf(TaskShareDto("2", "EDITOR_AND_VIEWER"))))
                authToken = login(user2Login)
            }
        )
    }

    @Test
    fun updateTask_viewerCannotUpdateTask() {
        testPatchEndpoint(
            uri = ApiPath.UPDATE_TASK.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = UpdateTaskDto(
                description = "updated by viewer"
            ),
            status = HttpStatusCode.NotFound,
            responseDto = Unit,
            authorize = {
                register(user2Register)
                registerAndLogin(user1Login)
            },
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto.copy(shares = listOf(TaskShareDto("2", "VIEWER"))))
                authToken = login(user2Login)
            }
        )
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }
}
