package com.marzec

import com.google.common.truth.Truth.assertThat
import com.marzec.core.CurrentTimeUtil
import com.marzec.fiteo.model.dto.ErrorDto
import com.marzec.fiteo.model.dto.LoginRequestDto
import com.marzec.todo.ApiPath
import com.marzec.todo.model.LeaveShareDto
import com.marzec.todo.model.TaskShareDto
import com.marzec.todo.model.UpdateTaskDto
import com.marzec.todo.model.UpdateTaskShareDto
import io.ktor.http.HttpStatusCode
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
            uri = ApiPath.ADD_TASK,
            dto = createTaskDto.copy(
                shares = listOf(TaskShareDto("3", "VIEWER"))
            ),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(id = 1, ownerId = 2, shares = listOf(TaskShareDto("3", "VIEWER"))),
            authorize = {
                register(user1Register)
                register(user2Register)
                login(user1Login)
            },
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
            },
            runRequestsAfter = {
                val tasksUser1 = getTasks()
                assertThat(tasksUser1.first().shares).isEqualTo(listOf(TaskShareDto("3", "VIEWER")))
                
                authToken = login(user2Login)
                val tasksUser2 = getTasks()
                assertThat(tasksUser2.first().shares).isEqualTo(listOf(TaskShareDto("3", "VIEWER")))
                assertThat(tasksUser2.first().description).isEqualTo("task")
            }
        )
    }

    @Test
    fun updateTask_ownerModifiesShares() {
        testPatchEndpoint(
            uri = ApiPath.UPDATE_TASK.replace("{id}", "1"),
            dto = UpdateTaskDto(
                shares = listOf(UpdateTaskShareDto("3", "EDITOR_AND_VIEWER"))
            ),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(id = 1, ownerId = 2, shares = listOf(TaskShareDto("3", "EDITOR_AND_VIEWER"))),
            authorize = {
                register(user1Register)
                register(user2Register)
                login(user1Login)
            },
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto.copy(shares = listOf(TaskShareDto("3", "VIEWER"))))
            },
            runRequestsAfter = {
                val tasksUser1 = getTasks()
                assertThat(tasksUser1.first().shares).isEqualTo(listOf(TaskShareDto("3", "EDITOR_AND_VIEWER")))
            }
        )
    }
    
    @Test
    fun updateTask_userUnshares() {
        testPostEndpoint(
            uri = ApiPath.LEAVE_SHARE,
            dto = LeaveShareDto(1),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(id = 1, ownerId = 2, shares = emptyList()),
            authorize = {
                register(user1Register)
                register(user2Register)
                login(user1Login)
            },
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto.copy(shares = listOf(TaskShareDto("3", "VIEWER"))))
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
            uri = ApiPath.UPDATE_TASK.replace("{id}", "1"),
            dto = UpdateTaskDto(
                description = "updated by editor"
            ),
            status = HttpStatusCode.OK,
            responseDto = taskDto.copy(id = 1, ownerId = 2, description = "updated by editor", shares = listOf(TaskShareDto("3", "EDITOR_AND_VIEWER"))),
            authorize = {
                register(user1Register)
                register(user2Register)
                login(user1Login)
            },
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto.copy(shares = listOf(TaskShareDto("3", "EDITOR_AND_VIEWER"))))
                authToken = login(user2Login)
            }
        )
    }

    @Test
    fun updateTask_viewerCannotUpdateTask() {
        testPatchEndpoint(
            uri = ApiPath.UPDATE_TASK.replace("{id}", "1"),
            dto = UpdateTaskDto(
                description = "updated by viewer"
            ),
            status = HttpStatusCode.InternalServerError,
            responseDto = ErrorDto("Action not permitted due to lack of editor permission"),
            authorize = {
                register(user1Register)
                register(user2Register)
                login(user1Login)
            },
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addTask(createTaskDto.copy(shares = listOf(TaskShareDto("3", "VIEWER"))))
                authToken = login(user2Login)
            }
        )
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }
}
