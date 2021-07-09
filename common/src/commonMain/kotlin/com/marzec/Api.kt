package com.marzec

object Api {

    const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

    object Args {
        const val ARG_ID = "id"
        const val ARG_USER_ID = "userId"
    }

    object Headers {
        const val AUTHORIZATION = "Authorization"
        const val AUTHORIZATION_TEST = "Authorization-Test"
    }

    object Auth {
        const val NAME = "fiteo_auth"
        const val TEST = "fiteo_test_auth"
    }
}