package com.marzec.model.exercises

import com.marzec.model.domain.Exercise
import com.marzec.repositories.ExercisesRepository

interface ExercisesModel {
    fun getExercises(): List<Exercise>
}

class ExercisesModelImpl(
        private val exercisesRepository: ExercisesRepository
) : ExercisesModel {

    override fun getExercises(): List<Exercise> {
        return exercisesRepository.getExercises()
    }
}