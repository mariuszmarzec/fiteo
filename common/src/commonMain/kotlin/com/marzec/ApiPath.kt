package com.marzec

object ApiPath {

    const val CURRENT_API_VERSION = "1"

    const val API = "api"

    const val ARG_ID = "id"

    const val APPLICATION_NAME = "fiteo"

    const val API_ROOT = "/$APPLICATION_NAME/$API/$CURRENT_API_VERSION/"

    const val USER = "$API_ROOT/user"
    const val EXERCISES_PAGE = "$API_ROOT/exercises/page"
    const val EXERCISES = "$API_ROOT/exercises"
    const val CATEGORIES = "$API_ROOT/categories"
    const val EQUIPMENT = "$API_ROOT/equipment"
    const val TRAININGS = "$API_ROOT/trainings"
    const val TRAINING_TEMPLATES = "$API_ROOT/trainings/templates"

    const val LOGIN = "$API_ROOT/login"
    const val LOGOUT = "$API_ROOT/logout"
}

object Headers {
    const val AUTHORIZATION = "Authorization"
}

object Auth {
    const val NAME = "fiteo_auth"
}