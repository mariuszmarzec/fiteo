package com.marzec.api

import com.marzec.model.dto.ExerciseDto

interface Api {

    fun getExercises(): List<ExerciseDto>
}