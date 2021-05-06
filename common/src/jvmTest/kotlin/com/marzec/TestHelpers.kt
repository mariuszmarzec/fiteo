package com.marzec

import com.marzec.database.DbSettings
import com.marzec.di.MainModule
import io.ktor.application.Application
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import io.mockk.mockk
import org.flywaydb.core.Flyway
import org.koin.core.module.Module
import org.koin.dsl.module

fun setupDb() {
    DbSettings.dbEndpoint = "jdbc:mysql://localhost:3306/fiteo_test_database?createDatabaseIfNotExist=TRUE"
    DbSettings.dbUser = "root"
    DbSettings.dbPassword = ""

    val flyway = Flyway.configure().dataSource(DbSettings.dbEndpoint, DbSettings.dbUser, DbSettings.dbPassword).load();

    flyway.clean()
    flyway.migrate()
}

fun <T> withMockTestApplication(
    mockConfiguration: Module.() -> Unit,
    applicationModule: Application.(List<Module>) -> Unit = Application::module,
    test: TestApplicationEngine.() -> T
) {
    val modules = MainModule.plus(module { mockConfiguration() })
    withTestApplication({ applicationModule(modules) }, test)
}

inline fun <reified T: Any> Module.factoryMock(crossinline mockConfiguration: (T) -> Unit) {
    factory(override = true) {
        mockk<T>().apply { mockConfiguration(this) }
    }
}
