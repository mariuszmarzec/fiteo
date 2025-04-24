package com.marzec.fiteo

import com.marzec.Api
import com.marzec.common.*
import com.marzec.di.Di
import com.marzec.fiteo.ApiPath.TRAINING_TEMPLATE_BY_ID
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
        template(api)
        putTemplate(api)
        removeTemplate(api)
        updateTemplate(api)

        createTraining(api)
        getTraining(api)
        getTrainings(api)
        removeTraining(api)
        updateTraining(api)
    }
    exercises(api)
    putExercise(api)
    updateExercise(api)
    deleteExercise(api)
    getExercise(api)

    exercisesPage()

    equipment(api)
    getEquipment(api)
    createEquipment(api)
    updateEquipment(api)
    deleteEquipment(api)

    categories(api)
    category(api)
    createCategory(api)
    updateCategory(api)
    deleteCategory(api)

    featureToggles(api)
    featureToggle(api)
    createFeatureToggle(api)
    updateFeatureToggle(api)
    deleteFeatureToggle(api)

    if (di.authToken == Api.Auth.TEST) {
        loadForceData(api)
    }
}

fun Route.createTraining(api: Controller) = postEndpoint(ApiPath.TRAININGS, api::createTraining)

fun Route.getTraining(api: Controller) = getByIdEndpoint(ApiPath.TRAINING, api::getTraining)

fun Route.getTrainings(api: Controller) = getAllEndpoint(ApiPath.TRAININGS, api::getTrainings)

fun Route.removeTraining(api: Controller) = deleteByIdEndpoint(ApiPath.TRAINING, api::removeTraining)

fun Route.updateTraining(api: Controller) = updateByIdEndpoint(ApiPath.TRAINING, api::updateTraining)

fun Route.templates(api: Controller) = getAllEndpoint(ApiPath.TRAINING_TEMPLATES, api::getTrainingTemplates)

fun Route.template(api: Controller) = getByIdEndpoint(ApiPath.TRAINING_TEMPLATE_BY_ID, api::getTrainingTemplate)

fun Route.putTemplate(api: Controller) = postEndpoint(ApiPath.TRAINING_TEMPLATE, api::addTrainingTemplate)

fun Route.removeTemplate(api: Controller) =
    deleteByIdEndpoint(TRAINING_TEMPLATE_BY_ID, api::removeTrainingTemplate)

fun Route.updateTemplate(api: Controller) =
    updateByIdEndpoint(TRAINING_TEMPLATE_BY_ID, api::updateTrainingTemplate)

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

fun Route.equipment(api: Controller) = getAllEndpoint(ApiPath.EQUIPMENT, api::getEquipment)

fun Route.getEquipment(api: Controller) = getByIdEndpoint(ApiPath.EQUIPMENT_BY_ID, api::getEquipmentById)

fun Route.createEquipment(api: Controller) = postEndpoint(ApiPath.EQUIPMENT, api::createEquipment)

fun Route.updateEquipment(api: Controller) = updateByIdEndpoint(ApiPath.EQUIPMENT_BY_ID, api::updateEquipment)

fun Route.deleteEquipment(api: Controller) = deleteByIdEndpoint(ApiPath.EQUIPMENT_BY_ID, api::deleteEquipment)

fun Route.categories(api: Controller) = getAllEndpoint(ApiPath.CATEGORIES, api::getCategories)

fun Route.category(api: Controller) = getByIdEndpoint(ApiPath.CATEGORY_BY_ID, api::getCategory)

fun Route.createCategory(api: Controller) = postEndpoint(ApiPath.CATEGORIES, api::createCategory)

fun Route.updateCategory(api: Controller) = updateByIdEndpoint(ApiPath.CATEGORY_BY_ID, api::updateCategory)

fun Route.deleteCategory(api: Controller) = deleteByIdEndpoint(ApiPath.CATEGORY_BY_ID, api::deleteCategory)

fun Route.featureToggles(api: Controller) = getAllEndpoint(ApiPath.FEATURE_TOGGLES, api::getFeatureToggles)

fun Route.featureToggle(api: Controller) = getByIdEndpoint(ApiPath.FEATURE_TOGGLE_BY_ID, api::getFeatureToggle)

fun Route.createFeatureToggle(api: Controller) = postEndpoint(ApiPath.FEATURE_TOGGLES, api::createFeatureToggle)

fun Route.updateFeatureToggle(api: Controller) = updateByIdEndpoint(ApiPath.FEATURE_TOGGLE_BY_ID, api::updateFeatureToggle)

fun Route.deleteFeatureToggle(api: Controller) = deleteByIdEndpoint(ApiPath.FEATURE_TOGGLE_BY_ID, api::deleteFeatureToggle)

fun Route.loadForceData(api: Controller) {
    get(ApiPath.LOAD_DATA) {
        respond(api.forceLoadData(HttpRequest(Unit)))
    }
}