package com.marzec.fiteo.repositories

import com.marzec.fiteo.model.domain.CreateExercise
import com.marzec.fiteo.model.domain.Exercise
import com.marzec.fiteo.model.domain.UpdateExercise

interface ExercisesRepository {
    fun getAll(): List<Exercise>
    fun addAll(exercises: List<Exercise>)
    fun createExercise(exercise: CreateExercise): Exercise
    fun updateExercise(id: Int, exercise: UpdateExercise): Exercise
    fun getExercise(id: Int): Exercise
    fun deleteExercise(id: Int): Exercise
}
