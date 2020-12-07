package com.marzec.repositories

import com.marzec.data.InitialDataLoader
import com.marzec.model.domain.*

interface ExercisesRepository {
    fun getExercises(): List<Exercise>
    fun getCategories(): List<Category>
    fun getEquipment(): List<Equipment>
    fun getTrainings(): List<Training>
    fun getTrainingTemplates(): List<TrainingTemplate>
}

class ExercisesRepositoryImpl(
        private val initialDataLoader: InitialDataLoader,
        private val categoriesRepository: CategoriesRepository
): ExercisesRepository {
    override fun getExercises(): List<Exercise> {
        return initialDataLoader.getExercises()
    }

    override fun getCategories(): List<Category> {
        return categoriesRepository.getAll()
    }

    override fun getEquipment(): List<Equipment> {
        return initialDataLoader.getEquipment()
    }

    override fun getTrainings(): List<Training> {
        return initialDataLoader.getTrainings()
    }

    override fun getTrainingTemplates(): List<TrainingTemplate> {
        return initialDataLoader.getTrainingTemplates()
    }
}