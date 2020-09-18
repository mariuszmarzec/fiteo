package com.marzec.database

import com.marzec.fiteo.BuildKonfig
import org.jetbrains.exposed.sql.*

object DbSettings {
    val database by lazy {
        Database.connect(
                url = BuildKonfig.DB_ENDPOINT,
                driver = "com.mysql.cj.jdbc.Driver",
                user = BuildKonfig.DB_USER,
                password = BuildKonfig.DB_PASSWORD
        )
    }
}