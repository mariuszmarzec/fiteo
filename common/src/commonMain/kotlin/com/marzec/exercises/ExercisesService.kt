package com.marzec.exercises

import com.marzec.model.domain.Category
import com.marzec.model.domain.Exercise
import com.marzec.repositories.ExercisesRepository

interface ExercisesService {
    fun getExercises(): List<Exercise>
    fun getCategories(): List<Category>
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
}