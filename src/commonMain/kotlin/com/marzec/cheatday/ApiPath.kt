package com.marzec.cheatday

object ApiPath {

    const val CURRENT_API_VERSION = "1"

    const val API = "api"

    const val ARG_ID = "id"

    const val APPLICATION_NAME = "cheat"

    const val API_ROOT = "/$APPLICATION_NAME/$API/$CURRENT_API_VERSION/"

    const val WEIGHTS = "$API_ROOT/weights"
    const val WEIGHT_BY_ID = "$API_ROOT/weights/{$ARG_ID}"
    @Deprecated("")
    const val WEIGHT = "$API_ROOT/weight"
    @Deprecated("")
    const val UPDATE_WEIGHT_DEPRECATED = "$API_ROOT/weight/{$ARG_ID}"
    @Deprecated("")
    const val REMOVE_WEIGHT_DEPRECATED = "$API_ROOT/weight/{$ARG_ID}"

}

/**
TODO CHEAT DAY WEIGHT unify endpoints:
get all - ok

post -
"$API_ROOT/weight" -> "$API_ROOT/weights"
backend fix - DONE
FIX on CLIENTS NOT_DONE
remove deprecated NOT_DONE

get by id -
LACKED -> "$API_ROOT/weights/{$ARG_ID}"
backend fix - DONE
FIX on CLIENTS NOT_DONE
remove deprecated NOT_DONE

update - UPDATE MAP?
"$API_ROOT/weights/{$ARG_ID}" -> "$API_ROOT/weights/{$ARG_ID}"
backend fix - DONE
FIX on CLIENTS NOT_DONE
remove deprecated NOT_DONE

remove -
"$API_ROOT/weights/{$ARG_ID}" -> "$API_ROOT/weights/{$ARG_ID}"
backend fix - DONE
FIX on CLIENTS NOT_DONE
remove deprecated NOT_DONE
 */