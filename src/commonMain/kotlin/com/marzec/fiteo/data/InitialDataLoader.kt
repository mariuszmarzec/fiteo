package com.marzec.fiteo.data

import com.marzec.core.Uuid
import com.marzec.fiteo.io.ExercisesReader
import com.marzec.fiteo.io.ResourceFileReader
import com.marzec.fiteo.model.domain.ExercisesData
import com.marzec.fiteo.model.domain.Training
import com.marzec.fiteo.model.domain.TrainingTemplate
import com.marzec.fiteo.model.dto.ExercisesFileDto
import com.marzec.fiteo.model.mappers.toDomain
import com.marzec.fiteo.repositories.CategoriesRepository
import com.marzec.fiteo.repositories.EquipmentRepository
import com.marzec.fiteo.repositories.ExercisesRepository

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
    private val exerciseFileMapper: ExerciseFileMapper
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
        val json = resourceFileReader.read("/exercises.json")
            ?: resourceFileReader.read("/example_exercises.json")!!
        val fileDto = reader.parse(json)
        exercisesData = exerciseFileMapper.toDomain(fileDto)
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

class ExerciseFileMapper(
    private val uuid: Uuid
) {
    fun toDomain(exercisesFileDto: ExercisesFileDto): ExercisesData = exercisesFileDto.toDomain(uuid)
}