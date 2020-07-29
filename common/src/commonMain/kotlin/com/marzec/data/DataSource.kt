package com.marzec.data

import com.marzec.io.ExercisesReader
import com.marzec.io.ResourceFileReader
import com.marzec.model.domain.*
import com.marzec.model.mappers.toDomain

interface DataSource {

    fun loadData()
    fun getExercises(): List<Exercise>
    fun getCategories(): List<Category>
    fun getEquipment(): List<Equipment>
    fun getTrainings(): List<Training>
    fun getTrainingTemplates(): List<TrainingTemplate>
}

class MemoryDataSource(
        private val reader: ExercisesReader,
        private val resourceFileReader: ResourceFileReader
) : DataSource {

    private val training = mutableListOf<Training>()
    private val trainingTemplate = mutableListOf<TrainingTemplate>()

    private var exercisesData: ExercisesData = ExercisesData(emptyList(), emptyList(), emptyList())

    override fun getExercises(): List<Exercise> {
        return exercisesData.exercises
    }

    override fun getCategories(): List<Category> {
        return exercisesData.categories
    }

    override fun getEquipment(): List<Equipment> {
        return exercisesData.equipment
    }

    override fun getTrainings(): List<Training> {
        return training
    }

    override fun getTrainingTemplates(): List<TrainingTemplate> {
        return trainingTemplate
    }

    override fun loadData() {
        exercisesData = reader.parse(resourceFileReader.read("/exercises.json")).toDomain()
    }
}