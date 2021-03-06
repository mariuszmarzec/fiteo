package com.marzec.fiteo.api

import com.marzec.fiteo.model.domain.CreateTrainingDto
import com.marzec.fiteo.model.domain.CreateTrainingTemplateDto
import com.marzec.fiteo.model.domain.TrainingDto
import com.marzec.fiteo.model.domain.TrainingTemplateDto
import com.marzec.fiteo.model.dto.CategoryDto
import com.marzec.fiteo.model.dto.EquipmentDto
import com.marzec.fiteo.model.dto.ExerciseDto
import com.marzec.fiteo.model.dto.LoginRequestDto
import com.marzec.fiteo.model.dto.RegisterRequestDto
import com.marzec.fiteo.model.dto.UserDto
import com.marzec.fiteo.model.http.HttpRequest
import com.marzec.fiteo.model.http.HttpResponse

interface Controller {

    fun getExercises(httpRequest: HttpRequest<Unit>): HttpResponse<List<ExerciseDto>>
    fun getCategories(httpRequest: HttpRequest<Unit>): HttpResponse<List<CategoryDto>>
    fun getEquipment(httpRequest: HttpRequest<Unit>): HttpResponse<List<EquipmentDto>>
    fun postLogin(httpRequest: HttpRequest<LoginRequestDto?>): HttpResponse<UserDto>
    fun getUser(httpRequest: HttpRequest<Unit>): HttpResponse<UserDto>
    fun postRegister(httpRequest: HttpRequest<RegisterRequestDto>): HttpResponse<UserDto>
    fun getTrainingTemplates(request: HttpRequest<Unit>): HttpResponse<List<TrainingTemplateDto>>
    fun addTrainingTemplate(request: HttpRequest<CreateTrainingTemplateDto>): HttpResponse<TrainingTemplateDto>
    fun updateTrainingTemplate(request: HttpRequest<CreateTrainingTemplateDto>): HttpResponse<TrainingTemplateDto>
    fun removeTrainingTemplate(request: HttpRequest<Unit>): HttpResponse<TrainingTemplateDto>
    fun createTraining(request: HttpRequest<Unit>): HttpResponse<TrainingDto>
    fun getTraining(request: HttpRequest<Unit>): HttpResponse<TrainingDto>
    fun getTrainings(request: HttpRequest<Unit>): HttpResponse<List<TrainingDto>>
    fun removeTraining(request: HttpRequest<Unit>): HttpResponse<TrainingDto>
    fun updateTraining(request: HttpRequest<CreateTrainingDto>): HttpResponse<TrainingDto>
}
