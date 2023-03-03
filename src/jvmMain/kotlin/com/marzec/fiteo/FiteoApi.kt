package com.marzec.fiteo

import com.marzec.Api
import com.marzec.common.*
import com.marzec.di.Di
import com.marzec.fiteo.api.Controller
import com.marzec.fiteo.model.http.HttpRequest
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.http.ContentType
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.fiteoApi(di: Di, api: Controller) {
    authenticate(di.authToken) {
        templates(api)
        putTemplate(api)
        removeTemplate(api)
        updateTemplate(api)

        createTraining(api)
        getTraining(api)
        getTrainings(api)
        removeTraining(api)
        updateTraining(api)
    }
    equipment(api)

    exercises(api)
    putExercise(api)
    updateExercise(api)
    deleteExercise(api)
    getExercise(api)

    exercisesPage()
    categories(api)

    if (di.authToken == Api.Auth.TEST) {
        loadForceData(api)
    }
}

fun Route.createTraining(api: Controller) = getByIdEndpoint(ApiPath.CREATE_TRAINING, api::createTraining)

fun Route.getTraining(api: Controller) = getByIdEndpoint(ApiPath.TRAINING, api::getTraining)

fun Route.getTrainings(api: Controller) = getAllEndpoint(ApiPath.TRAININGS, api::getTrainings)

fun Route.removeTraining(api: Controller) = deleteByIdEndpoint(ApiPath.TRAINING, api::removeTraining)

fun Route.updateTraining(api: Controller) = updateByIdEndpoint(ApiPath.TRAINING, api::updateTraining)

fun Route.templates(api: Controller) = getAllEndpoint(ApiPath.TRAINING_TEMPLATES, api::getTrainingTemplates)

fun Route.putTemplate(api: Controller) = postEndpoint(ApiPath.TRAINING_TEMPLATE, api::addTrainingTemplate)

fun Route.removeTemplate(api: Controller) =
    deleteByIdEndpoint(ApiPath.DELETE_TRAINING_TEMPLATES, api::removeTrainingTemplate)

fun Route.updateTemplate(api: Controller) =
    updateByIdEndpoint(ApiPath.UPDATE_TRAINING_TEMPLATES, api::updateTrainingTemplate)

fun Route.exercises(api: Controller) = getAllEndpoint(ApiPath.EXERCISES, api::getExercises)

fun Route.getExercise(api: Controller) = getByIdEndpoint(ApiPath.EXERCISE, api::getExercise)

fun Route.deleteExercise(api: Controller) = deleteByIdEndpoint(ApiPath.EXERCISE, api::deleteExercise)

fun Route.putExercise(api: Controller) = postEndpoint(ApiPath.EXERCISES, api::createExercise)

fun Route.updateExercise(api: Controller) = updateByIdEndpoint(ApiPath.EXERCISE, api::updateExercise)

fun Route.exercisesPage() {
    get(ApiPath.EXERCISES_PAGE) {
        call.respondText(
            this::class.java.classLoader.getResource("index.html")!!.readText(),
            ContentType.Text.Html
        )
    }
}

fun Route.categories(api: Controller) = getAllEndpoint(ApiPath.CATEGORIES, api::getCategories)

fun Route.equipment(api: Controller) = getAllEndpoint(ApiPath.EQUIPMENT, api::getEquipment)

fun Route.loadForceData(api: Controller) {
    get(ApiPath.LOAD_DATA) {
        dispatch(api.forceLoadData(HttpRequest(Unit)))
    }
}