package com.marzec

object Constants {

    const val CURRENT_API_VERSION = "1"

    const val API = "api"

    const val APPLICATION_NAME = "fiteo"

    const val API_ROOT = "/$APPLICATION_NAME/$API/$CURRENT_API_VERSION/"

    const val PATH_EXERCISES = "$API_ROOT/exercises"
    const val PATH_CATEGORIES = "$API_ROOT/categories"
    const val PATH_EQUIPMENT = "$API_ROOT/equipment"
    const val PATH_TRAININGS = "$API_ROOT/trainings"
    const val PATH_TRAINING_TEMPLATES = "$API_ROOT/trainings/templates"
}