package com.marzec.exercises

import com.marzec.model.domain.Category
import com.marzec.model.domain.Equipment
import com.marzec.model.domain.Exercise
import com.marzec.repositories.ExercisesRepository

interface ExercisesService {
    fun getExercises(): List<Exercise>
    fun getCategories(): List<Category>
    fun getEquipment(): List<Equipment>
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
}