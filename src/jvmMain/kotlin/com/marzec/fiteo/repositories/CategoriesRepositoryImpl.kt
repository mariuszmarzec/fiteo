package com.marzec.fiteo.repositories

import com.marzec.core.repository.CommonRepository
import com.marzec.database.CategoryEntity
import com.marzec.database.CategoryTable
import com.marzec.database.dbCall
import com.marzec.fiteo.model.domain.Category
import com.marzec.fiteo.model.domain.UpdateCategory
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder

class CategoriesRepositoryImpl(
    private val database: Database,
    private val repository: CommonRepository<String, Category, Category, UpdateCategory>
) : CategoriesRepository, CommonRepository<String, Category, Category, UpdateCategory> by repository {

    override fun getAll(): List<Category> = database.dbCall {
        CategoryEntity.all()
            .orderBy(CategoryTable.name to SortOrder.ASC)
            .map { it.toDomain() }
    }
}

