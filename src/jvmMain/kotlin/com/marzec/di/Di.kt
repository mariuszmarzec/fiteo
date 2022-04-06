package com.marzec.di

import com.marzec.fiteo.api.Controller
import com.marzec.fiteo.api.ControllerImpl
import com.marzec.cheatday.CheatDayController
import com.marzec.cheatday.CheatDayService
import com.marzec.cheatday.WeightsRepository
import com.marzec.core.TimeProvider
import com.marzec.core.Uuid
import com.marzec.core.UuidImpl
import com.marzec.fiteo.data.ExerciseFileMapper
import com.marzec.fiteo.data.InitialDataLoader
import com.marzec.fiteo.data.InitialDataLoaderImpl
import com.marzec.fiteo.services.AuthenticationService
import com.marzec.fiteo.services.AuthenticationServiceImpl
import com.marzec.fiteo.services.ExercisesService
import com.marzec.fiteo.services.ExercisesServiceImpl
import com.marzec.fiteo.services.TrainingService
import com.marzec.fiteo.services.TrainingServiceImpl
import com.marzec.fiteo.io.ExercisesReader
import com.marzec.fiteo.io.ExercisesReaderImpl
import com.marzec.fiteo.io.ResourceFileReader
import com.marzec.fiteo.io.ResourceFileReaderImpl
import com.marzec.fiteo.repositories.CachedSessionsRepository
import com.marzec.fiteo.repositories.CachedSessionsRepositoryImpl
import com.marzec.fiteo.repositories.CategoriesRepository
import com.marzec.fiteo.repositories.CategoriesRepositoryImpl
import com.marzec.fiteo.repositories.EquipmentRepository
import com.marzec.fiteo.repositories.EquipmentRepositoryImpl
import com.marzec.fiteo.repositories.ExercisesRepository
import com.marzec.fiteo.repositories.ExercisesRepositoryImpl
import com.marzec.fiteo.repositories.TrainingRepository
import com.marzec.fiteo.repositories.TrainingRepositoryImpl
import com.marzec.fiteo.repositories.TrainingTemplateRepository
import com.marzec.fiteo.repositories.TrainingTemplateRepositoryImpl
import com.marzec.fiteo.repositories.UserRepository
import com.marzec.fiteo.repositories.UserRepositoryImpl
import com.marzec.fiteo.repositories.WeightsRepositoryImpl
import com.marzec.todo.TaskConstraints
import com.marzec.todo.ToDoApiController
import com.marzec.todo.TodoService
import com.marzec.todo.TodoRepository
import com.marzec.todo.repositories.TodoRepositoryImpl
import com.marzec.trader.TraderApiController
import com.marzec.trader.TraderConstraints
import com.marzec.trader.TraderRepository
import com.marzec.trader.TraderService
import com.marzec.trader.repositories.TraderRepositoryImpl
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val SESSION_EXPIRATION_TIME = "SessionExpirationTime"

private const val MILLISECONDS_IN_SECOND = 1000
private const val SECONDS_IN_HOUR = 3600L
private const val HOURS_IN_DAY = 24
private const val DAYS_IN_MONTH = 31
private const val EXPIRATION_MONTHS_COUNT = 3

class Di(
    private val database: Database,
    val authToken: String
) : KoinComponent {

    val dataSource by inject<InitialDataLoader> { parametersOf(database, authToken) }
    val json by inject<Json> { parametersOf(database, authToken) }
    val cachedSessionsRepository by inject<CachedSessionsRepository> { parametersOf(database, authToken) }
    val api by inject<Controller> { parametersOf(database, authToken) }
    val cheatDayController by inject<CheatDayController> { parametersOf(database, authToken) }
    val todoController by inject<ToDoApiController> { parametersOf(database, authToken) }
    val traderApiController by inject<TraderApiController> { parametersOf(database, authToken) }
    val sessionExpirationTime by inject<Long>(qualifier = named(SESSION_EXPIRATION_TIME)) {
        parametersOf(database, authToken)
    }
}

val MainModule = module {
    single<Uuid> { UuidImpl() }

    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            useArrayPolymorphism = true

        }
    }

    single<TimeProvider> {
        object : TimeProvider {
            override fun currentTime(): LocalDateTime = com.marzec.core.currentTime().toKotlinLocalDateTime()
        }
    }

    single(qualifier = named(SESSION_EXPIRATION_TIME)) {
        EXPIRATION_MONTHS_COUNT * DAYS_IN_MONTH * HOURS_IN_DAY * SECONDS_IN_HOUR * MILLISECONDS_IN_SECOND
    }

    single { params -> ExerciseFileMapper(get { params }) }

    single<InitialDataLoader> { params ->
        InitialDataLoaderImpl(
            get { params },
            get { params },
            get { params },
            get { params },
            get { params },
            get { params })
    }

    factory<ExercisesReader> { params ->
        ExercisesReaderImpl(get { params })
    }

    factory<TrainingService> { params ->
        TrainingServiceImpl(
            get { params },
            get { params },
            get { params },
            get { params })
    }

    factory<TraderService> { params ->
        TraderService(
            get { params }
        )
    }

    factory<TrainingTemplateRepository> { params ->
        TrainingTemplateRepositoryImpl(get { params })
    }

    factory<Controller> { params ->
        ControllerImpl(
            get { params },
            get { params },
            get { params },
            get { params }
        )
    }

    factory<EquipmentRepository> { params -> EquipmentRepositoryImpl(get { params }) }

    factory<ResourceFileReader> { ResourceFileReaderImpl() }

    factory<AuthenticationService> { params -> AuthenticationServiceImpl(get { params }) }

    factory<UserRepository> { params -> UserRepositoryImpl(get { params }) }

    factory<ExercisesService> { params -> ExercisesServiceImpl(get { params }, get { params }, get { params }) }

    factory<ExercisesRepository> { params -> ExercisesRepositoryImpl(get { params }) }

    factory<CachedSessionsRepository> { params ->
        CachedSessionsRepositoryImpl(
            database = get { params }, sessionExpirationTime = get(named(SESSION_EXPIRATION_TIME))
        )
    }

    factory<CategoriesRepository> { params -> CategoriesRepositoryImpl(get { params }) }

    factory { params -> CheatDayController(get { params }) }

    factory { params -> CheatDayService(get { params }) }

    factory<WeightsRepository> { params -> WeightsRepositoryImpl(get { params }) }

    factory { params -> ToDoApiController(get { params }, get { params }) }

    factory { params -> TraderApiController(get { params }, get { params }) }

    factory { params -> TodoService(get { params }) }

    factory<TodoRepository> { params -> TodoRepositoryImpl(get { params }) }

    factory<TraderRepository> { params -> TraderRepositoryImpl(get { params }) }

    factory<TrainingRepository> { params -> TrainingRepositoryImpl(get { params }) }

    factory<TraderConstraints> { params -> TraderConstraints(get { params }) }

    factory<TaskConstraints> { params -> TaskConstraints() }
}
