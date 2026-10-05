package com.marzec.fiteo

import com.marzec.Api
import com.marzec.common.createHttpRequest
import com.marzec.common.respond
import com.marzec.di.Di
import com.marzec.fiteo.ApiPath.TRAINING_TEMPLATE_BY_ID
import com.marzec.fiteo.api.Controller
import com.marzec.fiteo.model.http.HttpRequest
import io.ktor.http.ContentType
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import kotlinx.serialization.json.JsonElement

fun Route.fiteoApi(di: Di, api: Controller) {
    authenticate(di.authToken) {
        users(api)
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
        addFcmToken(api)
        deleteFcmToken(api)
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

fun Route.users(api: Controller) = get(ApiPath.USERS) {
    respond(api.getUsers(createHttpRequest(Unit)))
}

fun Route.createTraining(api: Controller) = post(ApiPath.TRAININGS) {
    respond(api.createTraining(createHttpRequest(call.receive<CreateTrainingDto>())))
}

fun Route.getTraining(api: Controller) = get(ApiPath.TRAINING) {
    respond(api.getTraining(createHttpRequest(Unit)))
}

fun Route.getTrainings(api: Controller) = get(ApiPath.TRAININGS) {
    respond(api.getTrainings(createHttpRequest(Unit)))
}

fun Route.removeTraining(api: Controller) = delete(ApiPath.TRAINING) {
    respond(api.removeTraining(createHttpRequest(Unit)))
}

fun Route.updateTraining(api: Controller) = patch(ApiPath.TRAINING) {
    respond(api.updateTraining(createHttpRequest(call.receive<UpdateTrainingDto>())))
}

fun Route.templates(api: Controller) = get(ApiPath.TRAINING_TEMPLATES) {
    respond(api.getTrainingTemplates(createHttpRequest(Unit)))
}

fun Route.template(api: Controller) = get(TRAINING_TEMPLATE_BY_ID) {
    respond(api.getTrainingTemplate(createHttpRequest(Unit)))
}

fun Route.putTemplate(api: Controller) = post(ApiPath.TRAINING_TEMPLATE) {
    respond(api.addTrainingTemplate(createHttpRequest(call.receive<CreateTrainingTemplateDto>())))
}

fun Route.removeTemplate(api: Controller) = delete(TRAINING_TEMPLATE_BY_ID) {
    respond(api.removeTrainingTemplate(createHttpRequest(Unit)))
}

fun Route.updateTemplate(api: Controller) = patch(TRAINING_TEMPLATE_BY_ID) {
    respond(api.updateTrainingTemplate(createHttpRequest(call.receive<CreateTrainingTemplateDto>())))
}

fun Route.exercises(api: Controller) = get(ApiPath.EXERCISES) {
    respond(api.getExercises(createHttpRequest(Unit)))
}

fun Route.getExercise(api: Controller) = get(ApiPath.EXERCISE) {
    respond(api.getExercise(createHttpRequest(Unit)))
}

fun Route.deleteExercise(api: Controller) = delete(ApiPath.EXERCISE) {
    respond(api.deleteExercise(createHttpRequest(Unit)))
}

fun Route.putExercise(api: Controller) = post(ApiPath.EXERCISES) {
    respond(api.createExercise(createHttpRequest(call.receive<CreateExerciseDto>())))
}

fun Route.updateExercise(api: Controller) = patch(ApiPath.EXERCISE) {
    respond(api.updateExercise(createHttpRequest(call.receive<Map<String, JsonElement?>>())))
}

fun Route.exercisesPage() {
    get(ApiPath.EXERCISES_PAGE) {
        call.respondText(
            this::class.java.classLoader.getResource("index.html")!!.readText(),
            ContentType.Text.Html
        )
    }
}

fun Route.equipment(api: Controller) = get(ApiPath.EQUIPMENT) {
    respond(api.getEquipment(createHttpRequest(Unit)))
}

fun Route.getEquipment(api: Controller) = get(ApiPath.EQUIPMENT_BY_ID) {
    respond(api.getEquipmentById(createHttpRequest(Unit)))
}

fun Route.createEquipment(api: Controller) = post(ApiPath.EQUIPMENT) {
    respond(api.createEquipment(createHttpRequest(call.receive<EquipmentDto>())))
}

fun Route.updateEquipment(api: Controller) = patch(ApiPath.EQUIPMENT_BY_ID) {
    respond(api.updateEquipment(createHttpRequest(call.receive<Map<String, JsonElement?>>())))
}

fun Route.deleteEquipment(api: Controller) = delete(ApiPath.EQUIPMENT_BY_ID) {
    respond(api.deleteEquipment(createHttpRequest(Unit)))
}

fun Route.categories(api: Controller) = get(ApiPath.CATEGORIES) {
    respond(api.getCategories(createHttpRequest(Unit)))
}

fun Route.category(api: Controller) = get(ApiPath.CATEGORY_BY_ID) {
    respond(api.getCategory(createHttpRequest(Unit)))
}

fun Route.createCategory(api: Controller) = post(ApiPath.CATEGORIES) {
    respond(api.createCategory(createHttpRequest(call.receive<CategoryDto>())))
}

fun Route.updateCategory(api: Controller) = patch(ApiPath.CATEGORY_BY_ID) {
    respond(api.updateCategory(createHttpRequest(call.receive<Map<String, JsonElement?>>())))
}

fun Route.deleteCategory(api: Controller) = delete(ApiPath.CATEGORY_BY_ID) {
    respond(api.deleteCategory(createHttpRequest(Unit)))
}

fun Route.featureToggles(api: Controller) = get(ApiPath.FEATURE_TOGGLES) {
    respond(api.getFeatureToggles(createHttpRequest(Unit)))
}

fun Route.featureToggle(api: Controller) = get(ApiPath.FEATURE_TOGGLE_BY_ID) {
    respond(api.getFeatureToggle(createHttpRequest(Unit)))
}

fun Route.createFeatureToggle(api: Controller) = post(ApiPath.FEATURE_TOGGLES) {
    respond(api.createFeatureToggle(createHttpRequest(call.receive<NewFeatureToggleDto>())))
}

fun Route.updateFeatureToggle(api: Controller) = patch(ApiPath.FEATURE_TOGGLE_BY_ID) {
    respond(api.updateFeatureToggle(createHttpRequest(call.receive<Map<String, JsonElement?>>())))
}

fun Route.deleteFeatureToggle(api: Controller) = delete(ApiPath.FEATURE_TOGGLE_BY_ID) {
    respond(api.deleteFeatureToggle(createHttpRequest(Unit)))
}

fun Route.loadForceData(api: Controller) {
    get(ApiPath.LOAD_DATA) {
        respond(api.forceLoadData(HttpRequest(Unit)))
    }
}

fun Route.addFcmToken(api: Controller) = post(ApiPath.FCM_TOKEN) {
    respond(api.addFcmToken(createHttpRequest(call.receive<CreateFcmTokenDto>())))
}

fun Route.deleteFcmToken(api: Controller) = delete(ApiPath.DELETE_FCM_TOKEN) {
    respond(api.deleteFcmToken(createHttpRequest(Unit)))
}
