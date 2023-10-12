package com.marzec.fiteo.repositories

import com.marzec.fiteo.model.domain.Category
import com.marzec.fiteo.model.domain.UpdateCategory

interface CategoriesRepository {

    fun getAll(): List<Category>

    fun addAll(categories: List<Category>)

    fun create(category: Category): Category

    fun update(id: String, update: UpdateCategory): Category

    fun delete(id: String): Category
}

