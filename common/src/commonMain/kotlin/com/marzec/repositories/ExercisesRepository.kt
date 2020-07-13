package com.marzec.repositories

import com.marzec.data.DataSource
import com.marzec.model.domain.Category
import com.marzec.model.domain.Equipment
import com.marzec.model.domain.Exercise

interface ExercisesRepository {
    fun getExercises(): List<Exercise>
    fun getCategories(): List<Category>
    fun getEquipment(): List<Equipment>
}

class ExercisesRepositoryImpl(
        private val dataSource: DataSource
): ExercisesRepository {
    override fun getExercises(): List<Exercise> {
        return dataSource.getExercises()
    }

    override fun getCategories(): List<Category> {
        return dataSource.getCategories()
    }

    override fun getEquipment(): List<Equipment> {
        return dataSource.getEquipment()
    }
}