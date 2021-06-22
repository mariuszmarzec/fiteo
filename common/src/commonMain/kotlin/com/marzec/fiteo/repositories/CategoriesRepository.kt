package com.marzec.fiteo.repositories

import com.marzec.fiteo.model.domain.Category

interface CategoriesRepository {

    fun getAll(): List<Category>

    fun addAll(categories: List<Category>)
}

