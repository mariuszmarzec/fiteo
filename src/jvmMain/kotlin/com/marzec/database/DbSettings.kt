package com.marzec.database

import com.marzec.fiteo.BuildKonfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

object DbSettings {

    var dbEndpoint = BuildKonfig.DB_ENDPOINT
    var dbUser = BuildKonfig.DB_USER
    var dbPassword = BuildKonfig.DB_PASSWORD

    var dbTestEndpoint = BuildKonfig.DB_TEST_ENDPOINT
    var dbTestUser = BuildKonfig.DB_TEST_USER
    var dbTestPassword = BuildKonfig.DB_TEST_PASSWORD

    val database by lazy {
        createDatabase(dbEndpoint, dbUser, dbPassword)
    }

    val testDatabase by lazy {
        createDatabase(dbTestEndpoint, dbTestUser, dbTestPassword)
    }

    private fun createDatabase(
        endpoint: String,
        user: String,
        password: String
    ) = Database.connect(
        url = endpoint,
        driver = "com.mysql.cj.jdbc.Driver",
        user = user,
        password = password,
        databaseConfig = DatabaseConfig {
            useNestedTransactions = true
        }
    )
}

fun<T> Database.dbCall(statement: Transaction.() -> T) = transaction(this) {
    statement()
}
