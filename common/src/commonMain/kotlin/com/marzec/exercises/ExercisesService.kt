package com.marzec.exercises

import com.marzec.model.domain.*
import com.marzec.repositories.ExercisesRepository

interface ExercisesService {
    fun getExercises(): List<Exercise>
    fun getCategories(): List<Category>
    fun getEquipment(): List<Equipment>
    fun getTrainings(): List<Training>
    fun getTrainingTemplates(): List<TrainingTemplate>
}

class ExercisesServiceImpl(
        private val exercisesRepository: ExercisesRepository
) : ExercisesService {

    override fun getExercises(): List<Exercise> {
        return exercisesRepository.getExercises()
    }

    override fun getCategories(): List<Category> {
        return exercisesRepository.getCategories()
    }

    override fun getEquipment(): List<Equipment> {
        return exercisesRepository.getEquipment()
    }

    override fun getTrainings(): List<Training> {
        return exercisesRepository.getTrainings()
    }

    override fun getTrainingTemplates(): List<TrainingTemplate> {
        return exercisesRepository.getTrainingTemplates()
    }
}