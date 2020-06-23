package com.marzec.api

import com.marzec.model.domain.toDto
import com.marzec.model.dto.ExerciseDto
import com.marzec.exercises.ExercisesService
import com.marzec.model.dto.CategoryDto
import com.marzec.model.http.HttpResponse

class ControllerImpl(
        private val exercisesService: ExercisesService
) : Controller {
    override fun getCategories(): HttpResponse<List<CategoryDto>> = HttpResponse(exercisesService.getCategories().map { it.toDto() })

    override fun getExercises() = HttpResponse(exercisesService.getExercises().map { it.toDto() })
}