package com.marzec.fiteo

import com.marzec.Api

object ApiPath {

    private const val CURRENT_API_VERSION = "1"

    private const val API = "api"

    private const val APPLICATION_NAME = "fiteo"

    private const val API_ROOT = "/$APPLICATION_NAME/$API/$CURRENT_API_VERSION"

    const val TEST_API_PREFIX = "/test"

    const val USER = "$API_ROOT/user"
    const val USERS = "$API_ROOT/users"
    const val EXERCISES_PAGE = "$API_ROOT/exercises/page"

    const val EXERCISES = "$API_ROOT/exercises"
    const val EXERCISE = "$API_ROOT/exercises/{${Api.Args.ARG_ID}}"

    const val CATEGORIES = "$API_ROOT/categories"
    const val CATEGORY_BY_ID = "$API_ROOT/categories/{${Api.Args.ARG_ID}}"
    const val EQUIPMENT = "$API_ROOT/equipment"
    const val EQUIPMENT_BY_ID = "$API_ROOT/equipment/{${Api.Args.ARG_ID}}"

    const val TRAINING_TEMPLATES = "$API_ROOT/trainings/templates"
    const val TRAINING_TEMPLATE = "$API_ROOT/trainings/templates"
    const val TRAINING_TEMPLATE_BY_ID = "$API_ROOT/trainings/templates/{${Api.Args.ARG_ID}}"

    @Deprecated("")
    const val TRAINING_TEMPLATE_DEPRECATED = "$API_ROOT/trainings/template"
    @Deprecated("")
    const val DELETE_TRAINING_TEMPLATES_DEPRECATED = "$API_ROOT/trainings/template/{${Api.Args.ARG_ID}}"
    @Deprecated("")
    const val UPDATE_TRAINING_TEMPLATES_DEPRECATED = "$API_ROOT/trainings/template"

    @Deprecated("")
    const val CREATE_TRAINING_DEPRECATED = "$API_ROOT/trainings/template/{${Api.Args.ARG_ID}}/create-training"
    const val TRAINING = "$API_ROOT/trainings/{${Api.Args.ARG_ID}}"
    const val TRAININGS = "$API_ROOT/trainings"

    const val REGISTRATION = "$API_ROOT/registration"
    const val LOGIN = "$API_ROOT/login"
    const val LOGIN_BEARER = "$API_ROOT/login-bearer"
    const val LOGOUT = "$API_ROOT/logout"

    const val LOAD_DATA = "$API_ROOT/force-load"
}
/**
TODO FITEO unify endpoints:
CATEGORIES
get by id -
LACKED -> "$API_ROOT/categories/{$ARG_ID}"
backend fix - DONE

EQUIPMENT
get by id -
LACKED -> "$API_ROOT/categories/{$ARG_ID}"
backend fix - DONE


TRAINING_TEMPLATES
get all - ok

post -
$API_ROOT/trainings/template -> $API_ROOT/trainings/templates
backend fix - DONE
FIX on CLIENTS NOT_DONE
remove deprecated NOT_DONE

remove -
$API_ROOT/trainings/templates/{${Api.Args.ARG_ID}}" -> $API_ROOT/trainings/templates/{${Api.Args.ARG_ID}}
backend fix - DONE
FIX on CLIENTS NOT_DONE
remove deprecated NOT_DONE

get by id -
LACKED -> "$API_ROOT/trainings/templates/{${Api.Args.ARG_ID}}"
backend fix - DONE
FIX on CLIENTS NOT_DONE
remove deprecated NOT_DONE


CREATE_TRAINING
const val CREATE_TRAINING = "$API_ROOT/trainings/template/{${Api.Args.ARG_ID}}/create-training"
TO
const val TRAINING = "$API_ROOT/trainings"
 {
    val templateId: Int
 }
backend fix - DONE
FIX on CLIENTS NOT_DONE
remove deprecated NOT_DONE


$API_ROOT/trainings/templates/{${Api.Args.ARG_ID}}" -> $API_ROOT/trainings/templates/{${Api.Args.ARG_ID}}
backend fix - DONE
FIX on CLIENTS NOT_DONE
remove deprecated NOT_DONE

 */