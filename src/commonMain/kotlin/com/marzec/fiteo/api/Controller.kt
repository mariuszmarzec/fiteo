package com.marzec.fiteo.api

import com.marzec.fiteo.model.domain.UpdateTrainingDto
import com.marzec.fiteo.model.domain.CreateTrainingTemplateDto
import com.marzec.fiteo.model.domain.TrainingDto
import com.marzec.fiteo.model.domain.TrainingTemplateDto
import com.marzec.fiteo.model.dto.CategoryDto
import com.marzec.fiteo.model.dto.CreateExerciseDto
import com.marzec.fiteo.model.dto.EquipmentDto
import com.marzec.fiteo.model.dto.ExerciseDto
import com.marzec.fiteo.model.dto.LoginRequestDto
import com.marzec.fiteo.model.dto.RegisterRequestDto
import com.marzec.fiteo.model.dto.UserDto
import com.marzec.fiteo.model.http.HttpRequest
import com.marzec.fiteo.model.http.HttpResponse
import kotlinx.serialization.json.JsonElement

interface Controller {
    fun getExercises(request: HttpRequest<Unit>): HttpResponse<List<ExerciseDto>>
    fun getExercise(request: HttpRequest<Unit>): HttpResponse<ExerciseDto>
    fun deleteExercise(request: HttpRequest<Unit>): HttpResponse<ExerciseDto>
    fun createExercise(request: HttpRequest<CreateExerciseDto>): HttpResponse<ExerciseDto>
    fun updateExercise(request: HttpRequest<Map<String, JsonElement?>>): HttpResponse<ExerciseDto>
    fun getCategories(request: HttpRequest<Unit>): HttpResponse<List<CategoryDto>>
    fun getCategory(request: HttpRequest<Unit>): HttpResponse<CategoryDto>
    fun deleteCategory(request: HttpRequest<Unit>): HttpResponse<CategoryDto>
    fun createCategory(request: HttpRequest<CategoryDto>): HttpResponse<CategoryDto>
    fun updateCategory(request: HttpRequest<Map<String, JsonElement?>>): HttpResponse<CategoryDto>
    fun getEquipment(request: HttpRequest<Unit>): HttpResponse<List<EquipmentDto>>
    fun getEquipmentById(request: HttpRequest<Unit>): HttpResponse<EquipmentDto>
    fun deleteEquipment(request: HttpRequest<Unit>): HttpResponse<EquipmentDto>
    fun createEquipment(request: HttpRequest<EquipmentDto>): HttpResponse<EquipmentDto>
    fun updateEquipment(request: HttpRequest<Map<String, JsonElement?>>): HttpResponse<EquipmentDto>
    fun postLogin(request: HttpRequest<LoginRequestDto?>): HttpResponse<UserDto>
    fun getUser(request: HttpRequest<Unit>): HttpResponse<UserDto>
    fun getUsers(request: HttpRequest<Unit>): HttpResponse<List<UserDto>>
    fun postRegister(request: HttpRequest<RegisterRequestDto>): HttpResponse<UserDto>
    fun getTrainingTemplates(request: HttpRequest<Unit>): HttpResponse<List<TrainingTemplateDto>>
    fun getTrainingTemplate(request: HttpRequest<Unit>): HttpResponse<TrainingTemplateDto>
    fun addTrainingTemplate(request: HttpRequest<CreateTrainingTemplateDto>): HttpResponse<TrainingTemplateDto>
    fun updateTrainingTemplate(request: HttpRequest<CreateTrainingTemplateDto>): HttpResponse<TrainingTemplateDto>
    fun removeTrainingTemplate(request: HttpRequest<Unit>): HttpResponse<TrainingTemplateDto>
    fun createTraining(request: HttpRequest<Unit>): HttpResponse<TrainingDto>
    fun getTraining(request: HttpRequest<Unit>): HttpResponse<TrainingDto>
    fun getTrainings(request: HttpRequest<Unit>): HttpResponse<List<TrainingDto>>
    fun removeTraining(request: HttpRequest<Unit>): HttpResponse<TrainingDto>
    fun updateTraining(request: HttpRequest<UpdateTrainingDto>): HttpResponse<TrainingDto>
    fun forceLoadData(request: HttpRequest<Unit>): HttpResponse<Unit>
}
