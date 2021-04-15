package com.marzec.database

import com.marzec.fiteo.BuildKonfig
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object DbSettings {

    var dbEndpoint = BuildKonfig.DB_ENDPOINT
    var dbUser = BuildKonfig.DB_USER
    var dbPassword = BuildKonfig.DB_PASSWORD

    var dbTestEndpoint = BuildKonfig.DB_TEST_ENDPOINT
    var dbTestUser = BuildKonfig.DB_TEST_USER
    var dbTestPassword = BuildKonfig.DB_TEST_PASSWORD

    val database by lazy {
        Database.connect(
                url = dbEndpoint,
                driver = "com.mysql.cj.jdbc.Driver",
                user = dbUser,
                password = dbPassword
        )
    }

    val testDatabase by lazy {
        Database.connect(
                url = dbTestEndpoint,
                driver = "com.mysql.cj.jdbc.Driver",
                user = dbTestUser,
                password = dbTestPassword
        )
    }
}

fun<T> Database.dbCall(statement: Transaction.() -> T) = transaction(this) {
    statement()
}