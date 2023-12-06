package com.marzec.core.repository

import com.marzec.core.entity.CommonEntity
import com.marzec.core.entity.CommonEntityClass
import com.marzec.database.dbCall
import org.jetbrains.exposed.sql.Database

open class CommonRepositoryImpl<ID: Comparable<ID>, DOMAIN, CREATE, UPDATE, ENTITY : CommonEntity<ID, DOMAIN>>(
    private val entity: CommonEntityClass<ID, DOMAIN, CREATE, UPDATE, ENTITY>,
    private val database: Database
) : CommonRepository<ID, DOMAIN, CREATE, UPDATE> {

    override fun getAll(): List<DOMAIN> = database.dbCall {
        entity.all().map {
            it.toDomain()
        }
    }

    override fun getById(id: ID): DOMAIN = database.dbCall {
        entity.findByIdOrThrow(id).toDomain()
    }

    override fun addAll(items: List<CREATE>): List<DOMAIN> = database.dbCall {
        items.map {
            entity.create(it).toDomain()
        }
    }

    override fun create(item: CREATE): DOMAIN = database.dbCall {
        entity.create(item)
    }.toDomain()

    override fun update(id: ID, update: UPDATE): DOMAIN = database.dbCall {
        entity.update(id, update)
    }.toDomain()

    override fun delete(id: ID): DOMAIN = database.dbCall {
        val entity = entity.findByIdOrThrow(id)
        entity.delete()
        entity.toDomain()
    }
}
