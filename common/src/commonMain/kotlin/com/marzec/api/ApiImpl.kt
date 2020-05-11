package com.marzec.api

import com.marzec.model.domain.toDto
import com.marzec.model.dto.ExerciseDto
import com.marzec.model.exercises.ExercisesModel

class ApiImpl(
        private val exercisesModel: ExercisesModel
) : Api {
    override fun getExercises(): List<ExerciseDto> {
        return exercisesModel.getExercises().map { it.toDto() }
    }
}