package com.marzec.repositories

import com.marzec.model.domain.Category

interface CategoriesRepository {

    fun getAll(): List<Category>

    fun addAll(categories: List<Category>)
}

