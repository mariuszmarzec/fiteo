package com.marzec.di

import com.marzec.io.ExercisesReader
import com.marzec.io.ExercisesReaderImpl
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

object DI {

    fun provideJson() = Json(JsonConfiguration.Stable.copy(strictMode = false, useArrayPolymorphism = true))

    fun provideExercisesReader(): ExercisesReader = ExercisesReaderImpl(provideJson())

}