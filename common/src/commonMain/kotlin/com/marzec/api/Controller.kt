package com.marzec.api

import com.marzec.model.domain.TrainingDto
import com.marzec.model.domain.TrainingTemplateDto
import com.marzec.model.dto.CategoryDto
import com.marzec.model.dto.EquipmentDto
import com.marzec.model.dto.ExerciseDto
import com.marzec.model.dto.LoginRequestDto
import com.marzec.model.dto.SuccessDto
import com.marzec.model.http.HttpRequest
import com.marzec.model.http.HttpResponse

interface Controller {

    fun getExercises(): HttpResponse<List<ExerciseDto>>
    fun getCategories(): HttpResponse<List<CategoryDto>>
    fun getEquipment(): HttpResponse<List<EquipmentDto>>
    fun getTrainings(): HttpResponse<List<TrainingDto>>
    fun getTrainingTemplates(): HttpResponse<List<TrainingTemplateDto>>
    fun postLogin(httpRequest: HttpRequest<LoginRequestDto?>): HttpResponse<SuccessDto>
}