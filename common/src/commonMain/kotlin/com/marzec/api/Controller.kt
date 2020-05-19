package com.marzec.api

import com.marzec.model.dto.ExerciseDto
import com.marzec.model.http.HttpResponse

interface Controller {

    fun getExercises(): HttpResponse<List<ExerciseDto>>
}