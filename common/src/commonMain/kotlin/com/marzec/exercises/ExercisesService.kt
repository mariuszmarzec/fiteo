package com.marzec.exercises

import com.marzec.model.domain.*
import com.marzec.repositories.CategoriesRepository
import com.marzec.repositories.EquipmentRepository
import com.marzec.repositories.ExercisesRepository

interface ExercisesService {
    fun getExercises(): List<Exercise>
    fun getCategories(): List<Category>
    fun getEquipment(): List<Equipment>
    fun getTrainings(): List<Training>
    fun getTrainingTemplates(): List<TrainingTemplate>
}

class ExercisesServiceImpl(
        private val exercisesRepository: ExercisesRepository,
        private val categoriesRepository: CategoriesRepository,
        private val equipmentRepository: EquipmentRepository
) : ExercisesService {

    override fun getExercises(): List<Exercise> {
        return exercisesRepository.getAll()
    }

    override fun getCategories(): List<Category> {
        return categoriesRepository.getAll()
    }

    override fun getEquipment(): List<Equipment> {
        return equipmentRepository.getAll()
    }

    override fun getTrainings(): List<Training> {
        return emptyList()
    }

    override fun getTrainingTemplates(): List<TrainingTemplate> {
        return emptyList()
    }
}