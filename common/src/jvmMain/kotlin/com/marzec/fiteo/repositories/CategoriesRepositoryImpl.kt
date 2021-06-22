package com.marzec.fiteo.repositories

import com.marzec.database.CategoryEntity
import com.marzec.database.dbCall
import com.marzec.fiteo.model.domain.Category
import org.jetbrains.exposed.sql.Database

class CategoriesRepositoryImpl(private val database: Database) : CategoriesRepository {

    override fun getAll(): List<Category> = database.dbCall {
        CategoryEntity.all().map {
            it.toDomain()
        }
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
}