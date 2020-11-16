package com.marzec.repositories

import com.marzec.database.CategoryEntity
import com.marzec.database.dbCall
import com.marzec.model.domain.Category

class CategoriesRepositoryImpl : CategoriesRepository {

    override fun getAll(): List<Category> = dbCall {
        CategoryEntity.all().map {
            it.toDomain()
        }
    }

    override fun addAll(categories: List<Category>) {
        dbCall {
            categories.forEach {
                CategoryEntity.new(it.id) {
                    name = it.name
                }
            }
        }
    }
}