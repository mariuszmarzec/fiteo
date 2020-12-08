package com.marzec.data

import com.marzec.core.Uuid
import com.marzec.io.ExercisesReader
import com.marzec.io.ResourceFileReader
import com.marzec.model.domain.*
import com.marzec.model.mappers.toDomain
import com.marzec.repositories.CategoriesRepository
import com.marzec.repositories.EquipmentRepository
import com.marzec.repositories.ExercisesRepository

interface InitialDataLoader {

    fun loadData()
    fun getTrainings(): List<Training>
    fun getTrainingTemplates(): List<TrainingTemplate>
}

class InitialDataLoaderImpl(
        private val reader: ExercisesReader,
        private val resourceFileReader: ResourceFileReader,
        private val categoriesRepository: CategoriesRepository,
        private val equipmentRepository: EquipmentRepository,
        private val exercisesRepository: ExercisesRepository,
        private val uuid: Uuid
) : InitialDataLoader {

    private val training = mutableListOf<Training>()
    private val trainingTemplate = mutableListOf<TrainingTemplate>()

    private var exercisesData: ExercisesData = ExercisesData(emptyList(), emptyList(), emptyList())

    override fun getTrainings(): List<Training> {
        return training
    }

    override fun getTrainingTemplates(): List<TrainingTemplate> {
        return trainingTemplate
    }

    override fun loadData() {
        exercisesData = reader.parse(resourceFileReader.read("/exercises.json")).toDomain(uuid)
        if (categoriesRepository.getAll().isEmpty() ||
                equipmentRepository.getAll().isEmpty() ||
                exercisesRepository.getAll().isEmpty()
        ) {
            categoriesRepository.addAll(exercisesData.categories)
            equipmentRepository.addAll(exercisesData.equipment)
            exercisesRepository.addAll(exercisesData.exercises)
        }
    }
}