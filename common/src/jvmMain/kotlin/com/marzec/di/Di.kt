package com.marzec.di

import com.marzec.api.Controller
import com.marzec.api.ControllerImpl
import com.marzec.cheatday.CheatDayController
import com.marzec.cheatday.CheatDayService
import com.marzec.core.UuidImpl
import com.marzec.data.InitialDataLoader
import com.marzec.data.InitialDataLoaderImpl
import com.marzec.exercises.AuthenticationServiceImpl
import com.marzec.exercises.ExercisesService
import com.marzec.exercises.ExercisesServiceImpl
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
import com.marzec.repositories.TrainingRepositoryImpl
import com.marzec.repositories.TrainingTemplateRepositoryImpl
import com.marzec.repositories.UserRepositoryImpl
import com.marzec.repositories.WeightsRepositoryImpl
import com.marzec.todo.api.ToDoApiController
import com.marzec.todo.api.TodoService
import com.marzec.todo.repositories.TodoRepositoryImpl
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database

class Di(
        private val database: Database
) {

    private val uuid by lazy { UuidImpl() }

    private val INITIAL_DATA_LOADER: InitialDataLoader by lazy {
        InitialDataLoaderImpl(
                provideExercisesReader(),
                provideResourceFileReader(),
                provideCategoriesRepository(),
                provideEquipmentRepository(),
                provideExercisesRepository(),
                uuid
        )
    }

    private fun provideEquipmentRepository(): EquipmentRepository {
        return EquipmentRepositoryImpl(database)
    }

    private fun provideResourceFileReader(): ResourceFileReader {
        return ResourceFileReaderImpl()
    }

    fun provideJson() = Json {
        ignoreUnknownKeys = true
        isLenient = true
        useArrayPolymorphism = true
    }

    fun provideExercisesReader(): ExercisesReader = ExercisesReaderImpl(provideJson())

    fun provideApi(): Controller = ControllerImpl(
            provideExercisesModel(),
            provideAuthenticationService(),
            TrainingServiceImpl(
                    TrainingTemplateRepositoryImpl(database),
                    TrainingRepositoryImpl(database),
                    provideExercisesRepository(),
                    provideCategoriesRepository(),
                    provideEquipmentRepository()
            )
    )

    private fun provideAuthenticationService() = AuthenticationServiceImpl(provideUserRepository())

    private fun provideUserRepository() = UserRepositoryImpl(database)

    fun provideExercisesModel(): ExercisesService = ExercisesServiceImpl(
            provideExercisesRepository(),
            provideCategoriesRepository(),
            provideEquipmentRepository()
    )


    fun provideExercisesRepository(): ExercisesRepository = ExercisesRepositoryImpl(database)

    fun provideDataSource(): InitialDataLoader {
        return INITIAL_DATA_LOADER
    }

    fun provideCachedSessionsRepository(): CachedSessionsRepository = CachedSessionsRepositoryImpl(database)

    fun provideCategoriesRepository(): CategoriesRepository = CategoriesRepositoryImpl(database)

    fun provideCheatDayController(): CheatDayController = CheatDayController(CheatDayService(WeightsRepositoryImpl(database)))
    fun provideTodoController(): ToDoApiController = ToDoApiController(TodoService(TodoRepositoryImpl(database)))
}