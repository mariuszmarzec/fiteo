package com.marzec.repositories

import com.marzec.data.DataSource
import com.marzec.model.domain.*

interface ExercisesRepository {
    fun getExercises(): List<Exercise>
    fun getCategories(): List<Category>
    fun getEquipment(): List<Equipment>
    fun getTrainings(): List<Training>
    fun getTrainingTemplates(): List<TrainingTemplate>
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

    override fun getTrainings(): List<Training> {
        return dataSource.getTrainings()
    }

    override fun getTrainingTemplates(): List<TrainingTemplate> {
        return dataSource.getTrainingTemplates()
    }
}