package com.marzec.fiteo

import com.marzec.Api

object ApiPath {

    private const val CURRENT_API_VERSION = "1"

    private const val API = "api"

    private const val APPLICATION_NAME = "fiteo"

    private const val API_ROOT = "/$APPLICATION_NAME/$API/$CURRENT_API_VERSION"

    const val TEST_API_PREFIX = "/test"

    const val USER = "$API_ROOT/user"
    const val EXERCISES_PAGE = "$API_ROOT/exercises/page"
    const val EXERCISES = "$API_ROOT/exercises"
    const val CATEGORIES = "$API_ROOT/categories"
    const val EQUIPMENT = "$API_ROOT/equipment"

    const val TRAINING_TEMPLATES = "$API_ROOT/trainings/templates"
    const val TRAINING_TEMPLATE = "$API_ROOT/trainings/template"
    const val DELETE_TRAINING_TEMPLATES = "$API_ROOT/trainings/template/{${Api.Args.ARG_ID}}"
    const val UPDATE_TRAINING_TEMPLATES = "$API_ROOT/trainings/template"

    const val CREATE_TRAINING = "$API_ROOT/trainings/template/{${Api.Args.ARG_ID}}/create-training"
    const val TRAINING = "$API_ROOT/trainings/{${Api.Args.ARG_ID}}"
    const val TRAININGS = "$API_ROOT/trainings"

    const val REGISTRATION = "$API_ROOT/registration"
    const val LOGIN = "$API_ROOT/login"
    const val LOGOUT = "$API_ROOT/logout"

    const val LOAD_DATA = "$API_ROOT/force-load"
}
