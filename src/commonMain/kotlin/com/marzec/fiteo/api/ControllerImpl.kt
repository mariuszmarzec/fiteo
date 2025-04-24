package com.marzec.fiteo.api

import com.marzec.Api
import com.marzec.core.model.domain.toDto
import com.marzec.core.model.domain.toUpdateFeatureToggle
import com.marzec.core.model.dto.FeatureToggleDto
import com.marzec.core.model.dto.NewFeatureToggleDto
import com.marzec.core.model.dto.toDomain
import com.marzec.extensions.*
import com.marzec.fiteo.data.InitialDataLoader
import com.marzec.fiteo.model.domain.*
import com.marzec.fiteo.model.dto.CategoryDto
import com.marzec.fiteo.model.dto.CreateExerciseDto
import com.marzec.fiteo.model.dto.EquipmentDto
import com.marzec.fiteo.model.dto.ExerciseDto
import com.marzec.fiteo.model.dto.LoginRequestDto
import com.marzec.fiteo.model.dto.RegisterRequestDto
import com.marzec.fiteo.model.dto.UserDto
import com.marzec.fiteo.model.dto.toDomain
import com.marzec.fiteo.model.http.HttpRequest
import com.marzec.fiteo.model.http.HttpResponse
import com.marzec.fiteo.services.AuthenticationService
import com.marzec.fiteo.services.ExercisesService
import com.marzec.fiteo.services.FeatureTogglesService
import com.marzec.fiteo.services.TrainingService
import kotlinx.serialization.json.JsonElement

class ControllerImpl(
    private val exercisesService: ExercisesService,
    private val authenticationService: AuthenticationService,
    private val trainingService: TrainingService,
    private val featureTogglesService: FeatureTogglesService,
    private val initialDataLoader: InitialDataLoader
) : Controller {
    override fun getCategories(request: HttpRequest<Unit>): HttpResponse<List<CategoryDto>> =
        serviceCall { exercisesService.getCategories().map { it.toDto() } }

    override fun getCategory(request: HttpRequest<Unit>): HttpResponse<CategoryDto> = serviceCall {
        exercisesService.getCategory(
            id = request.getStringIdOrThrow(),
        ).toDto()
    }

    override fun deleteCategory(request: HttpRequest<Unit>): HttpResponse<CategoryDto> =
        serviceCall { exercisesService.deleteCategory(request.getStringIdOrThrow()).toDto() }

    override fun createCategory(request: HttpRequest<CategoryDto>): HttpResponse<CategoryDto> =
        serviceCall { exercisesService.createCategory(request.data.toDomain()).toDto() }

    override fun updateCategory(request: HttpRequest<Map<String, JsonElement?>>): HttpResponse<CategoryDto> =
        serviceCall {
            exercisesService.updateCategory(
                id = request.getStringIdOrThrow(),
                update = request.data.toUpdateCategory()
            ).toDto()
        }

    override fun getEquipment(request: HttpRequest<Unit>): HttpResponse<List<EquipmentDto>> =
        serviceCall { exercisesService.getEquipment().map { it.toDto() } }

    override fun getEquipmentById(request: HttpRequest<Unit>): HttpResponse<EquipmentDto> =
        serviceCall { exercisesService.getEquipmentById(request.getStringIdOrThrow()).toDto() }

    override fun deleteEquipment(request: HttpRequest<Unit>): HttpResponse<EquipmentDto> =
        serviceCall { exercisesService.deleteEquipment(request.getStringIdOrThrow()).toDto() }

    override fun createEquipment(request: HttpRequest<EquipmentDto>): HttpResponse<EquipmentDto> =
        serviceCall { exercisesService.createEquipment(request.data.toDomain()).toDto() }

    override fun updateEquipment(request: HttpRequest<Map<String, JsonElement?>>): HttpResponse<EquipmentDto> =
        serviceCall {
            exercisesService.updateEquipment(
                id = request.getStringIdOrThrow(),
                update = request.data.toUpdateEquipment()
            ).toDto()
        }

    override fun getExercises(request: HttpRequest<Unit>): HttpResponse<List<ExerciseDto>> =
        serviceCall { exercisesService.getExercises().map { it.toDto() } }

    override fun createExercise(request: HttpRequest<CreateExerciseDto>): HttpResponse<ExerciseDto> =
        serviceCall { exercisesService.createExercise(request.data.toDomain()).toDto() }

    override fun updateExercise(request: HttpRequest<Map<String, JsonElement?>>): HttpResponse<ExerciseDto> =
        serviceCall {
            exercisesService.updateExercise(
                id = request.getIntOrThrow(Api.Args.ARG_ID),
                exercise = request.data.toUpdateExercise()
            ).toDto()
        }

    override fun getExercise(request: HttpRequest<Unit>): HttpResponse<ExerciseDto> = serviceCall {
        exercisesService.getExercise(id = request.getIntOrThrow(Api.Args.ARG_ID)).toDto()
    }

    override fun deleteExercise(request: HttpRequest<Unit>): HttpResponse<ExerciseDto> = serviceCall {
        exercisesService.deleteExercise(id = request.getIntOrThrow(Api.Args.ARG_ID)).toDto()
    }

    override fun postLogin(request: HttpRequest<LoginRequestDto>): HttpResponse<UserDto> = serviceCall {
        val email = request.data.email.orEmpty()
        val password = request.data.password.orEmpty()
        authenticationService.checkPassword(email, password).toDto()
    }

    override fun getUser(request: HttpRequest<Unit>): HttpResponse<UserDto> = serviceCall {
        authenticationService.getUser(request.userIdOrThrow()).toDto()
    }

    override fun getUsers(request: HttpRequest<Unit>): HttpResponse<List<UserDto>> = serviceCall {
        authenticationService.getUsers().map { it.toDto() }
    }

    override fun postRegister(request: HttpRequest<RegisterRequestDto>): HttpResponse<UserDto> =
        with(request.data) {
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

    override fun getTrainingTemplate(request: HttpRequest<Unit>): HttpResponse<TrainingTemplateDto> =
        serviceCall {
            trainingService.getTrainingTemplate(
                request.userIdOrThrow(),
                request.getIntOrThrow(Api.Args.ARG_ID)
            ).toDto()
        }

    override fun addTrainingTemplate(
        request: HttpRequest<CreateTrainingTemplateDto>
    ): HttpResponse<TrainingTemplateDto> =
        serviceCall {
            trainingService.addTrainingTemplate(
                request.userIdOrThrow(),
                request.data.toDomain()
            ).toDto()
        }

    override fun updateTrainingTemplate(
        request: HttpRequest<CreateTrainingTemplateDto>
    ): HttpResponse<TrainingTemplateDto> =
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

    override fun createTrainingDeprecated(request: HttpRequest<Unit>): HttpResponse<TrainingDto> = serviceCall {
        trainingService.createTraining(
            request.userIdOrThrow(),
            request.getIntOrThrow(Api.Args.ARG_ID)
        ).toDto()
    }

    override fun createTraining(request: HttpRequest<CreateTrainingDto>): HttpResponse<TrainingDto> = serviceCall {
        trainingService.createTraining(
            request.userIdOrThrow(),
            request.data.templateTrainingId
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

    override fun updateTraining(request: HttpRequest<UpdateTrainingDto>): HttpResponse<TrainingDto> = serviceCall {
        trainingService.updateTraining(
            request.userIdOrThrow(),
            request.getIntOrThrow(Api.Args.ARG_ID),
            request.data.toDomain()
        ).toDto()
    }

    override fun forceLoadData(request: HttpRequest<Unit>): HttpResponse<Unit> = serviceCall {
        initialDataLoader.forceLoadData()
    }

    override fun getFeatureToggles(request: HttpRequest<Unit>): HttpResponse<List<FeatureToggleDto>> =
        serviceCall { featureTogglesService.getFeatureToggles().map { it.toDto() } }

    override fun getFeatureToggle(request: HttpRequest<Unit>): HttpResponse<FeatureToggleDto> = serviceCall {
        featureTogglesService.getFeatureToggle(
            id = request.getIntIdOrThrow(),
        ).toDto()
    }

    override fun deleteFeatureToggle(request: HttpRequest<Unit>): HttpResponse<FeatureToggleDto> =
        serviceCall { featureTogglesService.deleteFeatureToggle(request.getIntIdOrThrow()).toDto() }

    override fun createFeatureToggle(request: HttpRequest<NewFeatureToggleDto>): HttpResponse<FeatureToggleDto> =
        serviceCall { featureTogglesService.createFeatureToggle(request.data.toDomain()).toDto() }

    override fun updateFeatureToggle(request: HttpRequest<Map<String, JsonElement?>>): HttpResponse<FeatureToggleDto> =
        serviceCall {
            featureTogglesService.updateFeatureToggle(
                id = request.getIntIdOrThrow(),
                update = request.data.toUpdateFeatureToggle()
            ).toDto()
        }
}
