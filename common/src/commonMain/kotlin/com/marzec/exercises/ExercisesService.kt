package com.marzec.exercises

import com.marzec.model.domain.Exercise
import com.marzec.repositories.ExercisesRepository

interface ExercisesService {
    fun getExercises(): List<Exercise>
}

class ExercisesServiceImpl(
        private val exercisesRepository: ExercisesRepository
) : ExercisesService {

    override fun getExercises(): List<Exercise> {
        return exercisesRepository.getExercises()
    }
}