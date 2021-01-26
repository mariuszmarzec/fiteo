package com.marzec.api

import com.marzec.ApiPath
import com.marzec.exercises.AuthenticationService
import com.marzec.exercises.ExercisesService
import com.marzec.exercises.TrainingService
import com.marzec.extensions.getIntOrThrow
import com.marzec.extensions.serviceCall
import com.marzec.extensions.userIdOrThrow
import com.marzec.model.domain.CreateTrainingDto
import com.marzec.model.domain.CreateTrainingTemplateDto
import com.marzec.model.domain.Request
import com.marzec.model.domain.TrainingDto
import com.marzec.model.domain.TrainingTemplateDto
import com.marzec.model.domain.toDomain
import com.marzec.model.domain.toDto
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
        private val authenticationService: AuthenticationService,
        private val trainingService: TrainingService
) : Controller {
    override fun getCategories(): HttpResponse<List<CategoryDto>> =
            HttpResponse.Success(exercisesService.getCategories().map { it.toDto() })

    override fun getEquipment(): HttpResponse<List<EquipmentDto>> =
            HttpResponse.Success(exercisesService.getEquipment().map { it.toDto() })

    override fun getExercises(): HttpResponse.Success<List<ExerciseDto>> =
            HttpResponse.Success(exercisesService.getExercises().map { it.toDto() })

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

    override fun getTrainingTemplates(request: HttpRequest<Unit>): HttpResponse<List<TrainingTemplateDto>> =
            serviceCall {
                trainingService.getTrainingTemplates(
                        request.userIdOrThrow()
                ).map { it.toDto() }
            }

    override fun addTrainingTemplate(request: HttpRequest<CreateTrainingTemplateDto>): HttpResponse<TrainingTemplateDto> =
            serviceCall {
                trainingService.addTrainingTemplate(
                        request.userIdOrThrow(),
                        request.data.toDomain()
                ).toDto()
            }

    override fun updateTrainingTemplate(request: HttpRequest<CreateTrainingTemplateDto>): HttpResponse<TrainingTemplateDto> =
            serviceCall {
                trainingService.updateTrainingTemplate(
                        request.userIdOrThrow(),
                        request.data.toDomain()
                ).toDto()
            }

    override fun removeTrainingTemplate(request: HttpRequest<Unit>): HttpResponse<TrainingTemplateDto> =
            serviceCall {
                trainingService.removeTrainingTemplate(
                        request.userIdOrThrow(),
                        request.getIntOrThrow(ApiPath.ARG_ID)
                ).toDto()
            }

    override fun createTraining(request: HttpRequest<Unit>): HttpResponse<TrainingDto> = serviceCall {
        trainingService.createTraining(
                request.userIdOrThrow(),
                request.getIntOrThrow(ApiPath.ARG_ID)
        ).toDto()
    }

    override fun getTraining(request: HttpRequest<Unit>): HttpResponse<TrainingDto> = serviceCall {
        trainingService.getTraining(
                request.userIdOrThrow(),
                request.getIntOrThrow(ApiPath.ARG_ID)
        ).toDto()
    }

    override fun getTrainings(request: HttpRequest<Unit>): HttpResponse<List<TrainingDto>> = serviceCall {
        trainingService.getTrainings(
                request.userIdOrThrow()
        ).map { it.toDto() }
    }

    override fun removeTraining(request: HttpRequest<Unit>): HttpResponse<TrainingDto> = serviceCall {
        trainingService.removeTraining(
                request.userIdOrThrow(),
                request.getIntOrThrow(ApiPath.ARG_ID)
        ).toDto()
    }

    override fun updateTraining(request: HttpRequest<CreateTrainingDto>): HttpResponse<TrainingDto> = serviceCall {
        trainingService.updateTraining(
                request.userIdOrThrow(),
                request.getIntOrThrow(ApiPath.ARG_ID),
                request.data.toDomain()
        ).toDto()
    }
}