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
    val permission: String,
    val removed: Boolean = false
)

data class TaskShare(
    val userId: Int,
    val permission: SharePermission,
    val removed: Boolean = false
)

enum class SharePermission {
    VIEWER, EDITOR_AND_VIEWER
}

fun UpdateTaskShareDto.toDomain() = TaskShare(
    userId = userId.toInt(),
    permission = SharePermission.valueOf(permission.uppercase()),
    removed = removed
)

fun TaskShare.toDto() = TaskShareDto(
    userId = userId.toString(),
    permission = permission.name
)
