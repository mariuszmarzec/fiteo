package com.marzec.core.repository

import com.marzec.core.entity.WithUserEntity
import com.marzec.core.entity.WithUserEntityClass
import com.marzec.database.dbCall
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll

open class CommonWithUserRepositoryImpl<DOMAIN, CREATE, UPDATE, ENTITY : WithUserEntity<DOMAIN>>(
    private val entity: WithUserEntityClass<DOMAIN, CREATE, UPDATE, ENTITY>,
    private val database: Database
) : CommonWithUserRepository<DOMAIN, CREATE, UPDATE> {
    override fun getAll(userId: Int): List<DOMAIN> = database.dbCall {
        entity.table.selectAll()
            .andWhere { entity.withUserTable.userId.eq(userId) }.map {
                entity.wrapRow(it).toDomain()
            }
    }

    override fun getById(userId: Int, id: Int): DOMAIN = database.dbCall {
        val entity = entity.findByIdOrThrow(id)
        entity.belongsToUserOrThrow(userId)
        entity.toDomain()
    }


    override fun delete(userId: Int, id: Int): DOMAIN = database.dbCall {
        val entity = entity.findByIdOrThrow(id)
        entity.belongsToUserOrThrow(userId)
        entity.delete()
        entity.toDomain()
    }

    override fun update(userId: Int, id: Int, update: UPDATE): DOMAIN = database.dbCall {
        entity.findByIdOrThrow(id).apply {
            belongsToUserOrThrow(userId)
            entity.update(userId, this, update)
        }.toDomain()
    }


    override fun create(userId: Int, item: CREATE): DOMAIN = database.dbCall {
        entity.create(userId, item).toDomain()
    }

    override fun addAll(userId: Int, items: List<CREATE>): List<DOMAIN> = database.dbCall {
        items.map {
            entity.create(userId, it).toDomain()
        }
    }

}