package com.marzec.database

import com.marzec.fiteo.BuildKonfig
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object DbSettings {
    val database by lazy {
        Database.connect(
                url = BuildKonfig.DB_ENDPOINT,
                driver = "com.mysql.cj.jdbc.Driver",
                user = BuildKonfig.DB_USER,
                password = BuildKonfig.DB_PASSWORD
        )
    }

    val testDatabase by lazy {
        Database.connect(
                url = BuildKonfig.DB_TEST_ENDPOINT,
                driver = "com.mysql.cj.jdbc.Driver",
                user = BuildKonfig.DB_TEST_USER,
                password = BuildKonfig.DB_TEST_PASSWORD
        )
    }
}

fun<T> Database.dbCall(statement: Transaction.() -> T) = transaction(this) {
    statement()
}