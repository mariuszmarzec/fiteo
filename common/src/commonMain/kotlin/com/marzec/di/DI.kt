package com.marzec.di

import com.marzec.api.Api
import com.marzec.api.ApiImpl
import com.marzec.data.DataSource
import com.marzec.data.DataSourceImpl
import com.marzec.io.ExercisesReader
import com.marzec.io.ExercisesReaderImpl
import com.marzec.io.ResourceFileReader
import com.marzec.io.ResourceFileReaderImpl
import com.marzec.model.exercises.ExercisesModel
import com.marzec.model.exercises.ExercisesModelImpl
import com.marzec.repositories.ExercisesRepository
import com.marzec.repositories.ExercisesRepositoryImpl
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

object DI {

    private val dataSource: DataSource by lazy {
        DataSourceImpl(provideExercisesReader(), provideResourceFileReader())
    }

    private fun provideResourceFileReader(): ResourceFileReader {
        return ResourceFileReaderImpl()
    }

    fun provideJson() = Json(JsonConfiguration.Stable.copy(
            ignoreUnknownKeys = true,
            isLenient = true,
            useArrayPolymorphism = true
    ))

    fun provideExercisesReader(): ExercisesReader = ExercisesReaderImpl(provideJson())

    fun provideApi(): Api = ApiImpl(provideExercisesModel())

    fun provideExercisesModel(): ExercisesModel = ExercisesModelImpl(provideExercisesRepository())

    fun provideExercisesRepository(): ExercisesRepository = ExercisesRepositoryImpl(provideDataSource())

    fun provideDataSource(): DataSource {
        return dataSource
    }

}