package com.marzec.fiteo

import com.marzec.common.deleteByIdEndpoint
import com.marzec.common.getAllEndpoint
import com.marzec.common.getByIdEndpoint
import com.marzec.common.postEndpoint
import com.marzec.common.updateByIdEndpoint
import com.marzec.di.Di
import com.marzec.fiteo.api.Controller
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get

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
    exercisesPage()
    categories(api)
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
