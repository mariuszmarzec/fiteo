package com.marzec.api

import com.marzec.model.domain.CreateTrainingTemplateDto
import com.marzec.model.domain.TrainingDto
import com.marzec.model.domain.TrainingTemplateDto
import com.marzec.model.dto.CategoryDto
import com.marzec.model.dto.EquipmentDto
import com.marzec.model.dto.ExerciseDto
import com.marzec.model.dto.LoginRequestDto
import com.marzec.model.dto.RegisterRequestDto
import com.marzec.model.dto.UserDto
import com.marzec.model.http.HttpRequest
import com.marzec.model.http.HttpResponse

interface Controller {

    fun getExercises(): HttpResponse<List<ExerciseDto>>
    fun getCategories(): HttpResponse<List<CategoryDto>>
    fun getEquipment(): HttpResponse<List<EquipmentDto>>
    fun getTrainings(): HttpResponse<List<TrainingDto>>
    fun postLogin(httpRequest: HttpRequest<LoginRequestDto?>): HttpResponse<UserDto>
    fun getUser(httpRequest: HttpRequest<Unit>): HttpResponse<UserDto>
    fun postRegister(httpRequest: HttpRequest<RegisterRequestDto>): HttpResponse<UserDto>
    fun getTrainingTemplates(request: HttpRequest<Unit>): HttpResponse<List<TrainingTemplateDto>>
    fun addTrainingTemplate(request: HttpRequest<CreateTrainingTemplateDto>): HttpResponse<TrainingTemplateDto>
    fun updateTrainingTemplate(request: HttpRequest<CreateTrainingTemplateDto>): HttpResponse<TrainingTemplateDto>
    fun removeTrainingTemplate(request: HttpRequest<Unit>): HttpResponse<TrainingTemplateDto>
}