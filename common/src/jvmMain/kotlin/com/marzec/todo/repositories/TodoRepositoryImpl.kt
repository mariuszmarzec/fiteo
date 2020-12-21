package com.marzec.todo.repositories

import com.marzec.database.UserEntity
import com.marzec.database.dbCall
import com.marzec.database.findByIdOrThrow
import com.marzec.todo.database.ToDoListEntity
import com.marzec.todo.database.ToDoListTable
import com.marzec.todo.model.ToDoList
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll

class TodoRepositoryImpl : TodoRepository {

    override fun getLists(userId: Int): List<ToDoList> {
        return dbCall {
            ToDoListTable.selectAll()
                    .andWhere { ToDoListTable.userId.eq(userId) }
                    .map { ToDoListEntity.wrapRow(it).toDomain() }
        }
    }

    override fun addList(userId: Int, listName: String): ToDoList {
        return dbCall {
            ToDoListEntity.new {
                title = listName
                user = UserEntity.findByIdOrThrow(userId)
            }.toDomain()
        }
    }

    override fun removeList(userId: Int, listId: Int): ToDoList {
        return dbCall {
            val entity = ToDoListEntity.findByIdOrThrow(listId)
            entity.deleteIfBelongsToUserOrThrow(userId)
            entity.toDomain()
        }
    }
}