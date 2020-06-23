package com.marzec.data

import com.marzec.io.ExercisesReader
import com.marzec.io.ResourceFileReader
import com.marzec.model.domain.Category
import com.marzec.model.domain.Exercise
import com.marzec.model.domain.ExercisesData
import com.marzec.model.mappers.toDomain

interface DataSource {

    fun loadData()
    fun getExercises(): List<Exercise>
    fun getCategories(): List<Category>
}

class DataSourceImpl(
        private val reader: ExercisesReader,
        private val resourceFileReader: ResourceFileReader
) : DataSource {

    private var exercisesData: ExercisesData = ExercisesData(emptyList(), emptyList(), emptyList())

    override fun getExercises(): List<Exercise> {
        return exercisesData.exercises
    }

    override fun getCategories(): List<Category> {
        return exercisesData.categories
    }

    override fun loadData() {
        exercisesData = reader.parse(resourceFileReader.read("/exercises.json")).toDomain()
    }
}