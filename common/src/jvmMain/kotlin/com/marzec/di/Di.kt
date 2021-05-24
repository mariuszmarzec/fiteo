package com.marzec.di

import com.marzec.api.Controller
import com.marzec.api.ControllerImpl
import com.marzec.cheatday.CheatDayController
import com.marzec.cheatday.CheatDayService
import com.marzec.cheatday.WeightsRepository
import com.marzec.core.TimeProvider
import com.marzec.core.Uuid
import com.marzec.core.UuidImpl
import com.marzec.data.ExerciseFileMapper
import com.marzec.data.InitialDataLoader
import com.marzec.data.InitialDataLoaderImpl
import com.marzec.exercises.AuthenticationService
import com.marzec.exercises.AuthenticationServiceImpl
import com.marzec.exercises.ExercisesService
import com.marzec.exercises.ExercisesServiceImpl
import com.marzec.exercises.TrainingService
import com.marzec.exercises.TrainingServiceImpl
import com.marzec.io.ExercisesReader
import com.marzec.io.ExercisesReaderImpl
import com.marzec.io.ResourceFileReader
import com.marzec.io.ResourceFileReaderImpl
import com.marzec.repositories.CachedSessionsRepository
import com.marzec.repositories.CachedSessionsRepositoryImpl
import com.marzec.repositories.CategoriesRepository
import com.marzec.repositories.CategoriesRepositoryImpl
import com.marzec.repositories.EquipmentRepository
import com.marzec.repositories.EquipmentRepositoryImpl
import com.marzec.repositories.ExercisesRepository
import com.marzec.repositories.ExercisesRepositoryImpl
import com.marzec.repositories.TrainingRepository
import com.marzec.repositories.TrainingRepositoryImpl
import com.marzec.repositories.TrainingTemplateRepository
import com.marzec.repositories.TrainingTemplateRepositoryImpl
import com.marzec.repositories.UserRepository
import com.marzec.repositories.UserRepositoryImpl
import com.marzec.repositories.WeightsRepositoryImpl
import com.marzec.todo.api.ToDoApiController
import com.marzec.todo.api.TodoService
import com.marzec.todo.repositories.TodoRepository
import com.marzec.todo.repositories.TodoRepositoryImpl
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

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

    single<ExerciseFileMapper> { params -> ExerciseFileMapper(get { params }) }

    single<InitialDataLoader> { params -> InitialDataLoaderImpl(get { params }, get { params }, get { params }, get { params }, get { params }, get { params }) }

    factory<ExercisesReader> { params ->
        ExercisesReaderImpl(get { params })
    }

    factory<TrainingService> { params -> TrainingServiceImpl(get { params }, get { params }, get { params }, get { params }, get { params }, get { params }) }

    factory<TrainingTemplateRepository> { params ->
        TrainingTemplateRepositoryImpl(get { params })
    }

    factory<Controller> { params -> ControllerImpl(get { params }, get { params }, get { params }) }

    factory<EquipmentRepository> { params -> EquipmentRepositoryImpl(get { params }) }

    factory<ResourceFileReader> { ResourceFileReaderImpl() }

    factory<AuthenticationService> { params -> AuthenticationServiceImpl(get { params }) }

    factory<UserRepository> { params -> UserRepositoryImpl(get { params }) }

    factory<ExercisesService> { params -> ExercisesServiceImpl(get { params }, get { params }, get { params }) }

    factory<ExercisesRepository> { params -> ExercisesRepositoryImpl(get { params }) }

    factory<CachedSessionsRepository> { params -> CachedSessionsRepositoryImpl(get { params }) }

    factory<CategoriesRepository> { params -> CategoriesRepositoryImpl(get { params }) }

    factory { params -> CheatDayController(get { params }) }

    factory { params -> CheatDayService(get { params }) }

    factory<WeightsRepository> { params -> WeightsRepositoryImpl(get { params }) }

    factory { params -> ToDoApiController(get { params }) }

    factory { params -> TodoService(get { params }) }

    factory<TodoRepository> { params -> TodoRepositoryImpl(get { params }) }

    factory<TrainingRepository> { params -> TrainingRepositoryImpl(get { params }) }
}
