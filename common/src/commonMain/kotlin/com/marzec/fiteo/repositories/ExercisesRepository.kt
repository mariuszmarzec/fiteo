package com.marzec.repositories

import com.marzec.model.domain.Exercise
import com.marzec.model.domain.Training
import com.marzec.model.domain.TrainingTemplate

interface ExercisesRepository {
    fun getAll(): List<Exercise>
    fun addAll(exercises: List<Exercise>)
}