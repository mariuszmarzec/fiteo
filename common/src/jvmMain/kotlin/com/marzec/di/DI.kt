package com.marzec.di

import com.marzec.api.Controller
import com.marzec.api.ControllerImpl
import com.marzec.core.UuidImpl
import com.marzec.data.DataSource
import com.marzec.data.MemoryDataSource
import com.marzec.exercises.AuthenticationService
import com.marzec.exercises.AuthenticationServiceImpl
import com.marzec.io.ExercisesReader
import com.marzec.io.ExercisesReaderImpl
import com.marzec.io.ResourceFileReader
import com.marzec.io.ResourceFileReaderImpl
import com.marzec.exercises.ExercisesService
import com.marzec.exercises.ExercisesServiceImpl
import com.marzec.repositories.CachedSessionsRepository
import com.marzec.repositories.CachedSessionsRepositoryImpl
import com.marzec.repositories.ExercisesRepository
import com.marzec.repositories.ExercisesRepositoryImpl
import com.marzec.repositories.UserRepository
import com.marzec.repositories.UserRepositoryImpl
import kotlinx.serialization.json.Json

object DI {

    private val uuid by lazy { UuidImpl() }

    private val dataSource: DataSource by lazy {
        MemoryDataSource(provideExercisesReader(), provideResourceFileReader(), uuid)
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

    fun provideApi(): Controller = ControllerImpl(provideExercisesModel(), provideAuthenticationService())

    private fun provideAuthenticationService() = AuthenticationServiceImpl(provideUserRepository())

    private fun provideUserRepository() = UserRepositoryImpl()

    fun provideExercisesModel(): ExercisesService = ExercisesServiceImpl(provideExercisesRepository())

    fun provideExercisesRepository(): ExercisesRepository = ExercisesRepositoryImpl(provideDataSource())

    fun provideDataSource(): DataSource {
        return dataSource
    }

    fun provideCachedSessionsRepository(): CachedSessionsRepository = CachedSessionsRepositoryImpl()

}