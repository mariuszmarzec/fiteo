package com.marzec.fiteo.repositories

import com.marzec.database.CategoryEntity
import com.marzec.database.dbCall
import com.marzec.database.findByIdOrThrow
import com.marzec.fiteo.model.domain.Category
import com.marzec.fiteo.model.domain.UpdateCategory
import org.jetbrains.exposed.sql.Database
import com.marzec.extensions.update as updateExt

class CategoriesRepositoryImpl(private val database: Database) : CategoriesRepository {

    override fun getAll(): List<Category> = database.dbCall {
        CategoryEntity.all().map {
            it.toDomain()
        }
    }

    override fun getById(id: String): Category = database.dbCall {
        CategoryEntity.findByIdOrThrow(id).toDomain()
    }

    override fun addAll(categories: List<Category>) {
        database.dbCall {
            categories.forEach {
                CategoryEntity.new(it.id) {
                    name = it.name
                }
            }
        }
    }

    override fun create(category: Category): Category = database.dbCall {
        CategoryEntity.new(category.id) {
            name = category.name
        }
    }.toDomain()

    override fun update(id: String, update: UpdateCategory): Category = database.dbCall {
        CategoryEntity.findByIdOrThrow(id).apply {
            updateExt(this::name, update.name)
        }
    }.toDomain()

    override fun delete(id: String): Category = database.dbCall {
        val entity = CategoryEntity.findByIdOrThrow(id)
        entity.delete()
        entity.toDomain()
    }
}
