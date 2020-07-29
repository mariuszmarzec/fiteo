package com.marzec.api

import com.marzec.model.domain.toDto
import com.marzec.model.dto.ExerciseDto
import com.marzec.exercises.ExercisesService
import com.marzec.model.domain.TrainingDto
import com.marzec.model.domain.TrainingTemplateDto
import com.marzec.model.dto.CategoryDto
import com.marzec.model.dto.EquipmentDto
import com.marzec.model.http.HttpResponse

class ControllerImpl(
        private val exercisesService: ExercisesService
) : Controller {
    override fun getCategories(): HttpResponse<List<CategoryDto>> = HttpResponse(exercisesService.getCategories().map { it.toDto() })

    override fun getEquipment(): HttpResponse<List<EquipmentDto>> = HttpResponse(exercisesService.getEquipment().map { it.toDto() })

    override fun getExercises() = HttpResponse(exercisesService.getExercises().map { it.toDto() })

    override fun getTrainings(): HttpResponse<List<TrainingDto>> = HttpResponse(exercisesService.getTrainings().map { it.toDto() })

    override fun getTrainingTemplates(): HttpResponse<List<TrainingTemplateDto>> = HttpResponse(exercisesService.getTrainingTemplates().map { it.toDto() })
}