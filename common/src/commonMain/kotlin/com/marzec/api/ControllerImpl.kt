package com.marzec.api

import com.marzec.ApiPath
import com.marzec.exceptions.HttpException
import com.marzec.exercises.AuthenticationService
import com.marzec.model.domain.toDto
import com.marzec.exercises.ExercisesService
import com.marzec.model.domain.Request
import com.marzec.model.domain.TrainingDto
import com.marzec.model.domain.TrainingTemplateDto
import com.marzec.model.dto.CategoryDto
import com.marzec.model.dto.EquipmentDto
import com.marzec.model.dto.ErrorDto
import com.marzec.model.dto.ExerciseDto
import com.marzec.model.dto.LoginRequestDto
import com.marzec.model.dto.RegisterRequestDto
import com.marzec.model.dto.UserDto
import com.marzec.model.http.HttpRequest
import com.marzec.model.http.HttpResponse

class ControllerImpl(
        private val exercisesService: ExercisesService,
        private val authenticationService: AuthenticationService
) : Controller {
    override fun getCategories(): HttpResponse<List<CategoryDto>> =
            HttpResponse.Success(exercisesService.getCategories().map { it.toDto() })

    override fun getEquipment(): HttpResponse<List<EquipmentDto>> =
            HttpResponse.Success(exercisesService.getEquipment().map { it.toDto() })

    override fun getExercises(): HttpResponse.Success<List<ExerciseDto>> =
            HttpResponse.Success(exercisesService.getExercises().map { it.toDto() })

    override fun getTrainings(): HttpResponse<List<TrainingDto>> =
            HttpResponse.Success(exercisesService.getTrainings().map { it.toDto() })

    override fun getTrainingTemplates(): HttpResponse<List<TrainingTemplateDto>> =
            HttpResponse.Success(exercisesService.getTrainingTemplates().map { it.toDto() })

    override fun postLogin(httpRequest: HttpRequest<LoginRequestDto?>): HttpResponse<UserDto> {
        val email = httpRequest.data?.email.orEmpty()
        val password = httpRequest.data?.password.orEmpty()
        return when (val result = authenticationService.checkPassword(email, password)) {
            is Request.Success -> HttpResponse.Success(result.data.toDto())
            is Request.Error -> HttpResponse.Error(ErrorDto(result.reason))
        }
    }

    override fun getUser(httpRequest: HttpRequest<Unit>): HttpResponse<UserDto> {
        val userId = httpRequest.parameters[ApiPath.ARG_ID]?.toIntOrNull()
                ?: return HttpResponse.Error(ErrorDto("Argument ${ApiPath.ARG_ID} is not integer"))
        return when (val result = authenticationService.getUser(userId)) {
            is Request.Success -> HttpResponse.Success(result.data.toDto())
            is Request.Error -> HttpResponse.Error(ErrorDto(result.reason))
        }
    }

    override fun postRegister(httpRequest: HttpRequest<RegisterRequestDto>): HttpResponse<UserDto> =
            with(httpRequest.data) {
                serviceCall {
                    authenticationService.register(email, password, repeatedPassword).toDto()
                }
            }

    private fun <T> serviceCall(call: () -> T): HttpResponse<T> {
        return try {
            HttpResponse.Success(call())
        } catch (e: Exception) {
            when (e) {
                is HttpException -> HttpResponse.Error(ErrorDto(e.message.orEmpty()), e.httpStatus)
                else -> HttpResponse.Error(ErrorDto(e.message.orEmpty()), 500)
            }
        }
    }
}