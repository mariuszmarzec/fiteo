package com.marzec.fiteo.api

import com.marzec.Api
import com.marzec.fiteo.services.AuthenticationService
import com.marzec.fiteo.services.ExercisesService
import com.marzec.fiteo.services.TrainingService
import com.marzec.extensions.getIntOrThrow
import com.marzec.extensions.serviceCall
import com.marzec.extensions.userIdOrThrow
import com.marzec.fiteo.model.domain.CreateTrainingDto
import com.marzec.fiteo.model.domain.CreateTrainingTemplateDto
import com.marzec.fiteo.model.domain.Request
import com.marzec.fiteo.model.domain.TrainingDto
import com.marzec.fiteo.model.domain.TrainingTemplateDto
import com.marzec.fiteo.model.domain.toDomain
import com.marzec.fiteo.model.domain.toDto
import com.marzec.fiteo.model.dto.CategoryDto
import com.marzec.fiteo.model.dto.EquipmentDto
import com.marzec.fiteo.model.dto.ErrorDto
import com.marzec.fiteo.model.dto.ExerciseDto
import com.marzec.fiteo.model.dto.LoginRequestDto
import com.marzec.fiteo.model.dto.RegisterRequestDto
import com.marzec.fiteo.model.dto.UserDto
import com.marzec.fiteo.model.http.HttpRequest
import com.marzec.fiteo.model.http.HttpResponse

class ControllerImpl(
    private val exercisesService: ExercisesService,
    private val authenticationService: AuthenticationService,
    private val trainingService: TrainingService
) : Controller {
    override fun getCategories(httpRequest: HttpRequest<Unit>): HttpResponse<List<CategoryDto>> =
            HttpResponse.Success(exercisesService.getCategories().map { it.toDto() })

    override fun getEquipment(httpRequest: HttpRequest<Unit>): HttpResponse<List<EquipmentDto>> =
            HttpResponse.Success(exercisesService.getEquipment().map { it.toDto() })

    override fun getExercises(httpRequest: HttpRequest<Unit>): HttpResponse.Success<List<ExerciseDto>> =
            HttpResponse.Success(exercisesService.getExercises().map { it.toDto() })

    override fun postLogin(httpRequest: HttpRequest<LoginRequestDto?>): HttpResponse<UserDto> {
        val email = httpRequest.data?.email.orEmpty()
        val password = httpRequest.data?.password.orEmpty()
        return when (val result = authenticationService.checkPassword(email, password)) {
            is Request.Success -> HttpResponse.Success(result.data.toDto())
            is Request.Error -> HttpResponse.Error(ErrorDto(result.reason))
        }
    }

    override fun getUser(httpRequest: HttpRequest<Unit>): HttpResponse<UserDto> = serviceCall {
        authenticationService.getUser(httpRequest.userIdOrThrow()).toDto()
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
                        request.getIntOrThrow(Api.Args.ARG_ID)
                ).toDto()
            }

    override fun createTraining(request: HttpRequest<Unit>): HttpResponse<TrainingDto> = serviceCall {
        trainingService.createTraining(
                request.userIdOrThrow(),
                request.getIntOrThrow(Api.Args.ARG_ID)
        ).toDto()
    }

    override fun getTraining(request: HttpRequest<Unit>): HttpResponse<TrainingDto> = serviceCall {
        trainingService.getTraining(
                request.userIdOrThrow(),
                request.getIntOrThrow(Api.Args.ARG_ID)
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
                request.getIntOrThrow(Api.Args.ARG_ID)
        ).toDto()
    }

    override fun updateTraining(request: HttpRequest<CreateTrainingDto>): HttpResponse<TrainingDto> = serviceCall {
        trainingService.updateTraining(
                request.userIdOrThrow(),
                request.getIntOrThrow(Api.Args.ARG_ID),
                request.data.toDomain()
        ).toDto()
    }
}