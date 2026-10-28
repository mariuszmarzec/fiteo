package com.marzec.todo.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskShareDto(
    val userId: String,
    val permission: String
)

@Serializable
data class UpdateTaskShareDto(
    val userId: String,
    val permission: String
)

data class TaskShare(
    val userId: Int,
    val permission: SharePermission
)

enum class SharePermission {
    VIEWER, EDITOR_AND_VIEWER
}

fun UpdateTaskShareDto.toDomain() = TaskShare(
    userId = userId.toInt(),
    permission = SharePermission.valueOf(permission.uppercase())
)

fun TaskShare.toDto() = TaskShareDto(
    userId = userId.toString(),
    permission = permission.name
)