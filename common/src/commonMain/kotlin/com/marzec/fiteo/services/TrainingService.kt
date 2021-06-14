package com.marzec.exercises

import com.marzec.core.TimeProvider
import com.marzec.model.domain.*
import com.marzec.fiteo.repositories.*

interface TrainingService {
    fun getTrainingTemplates(userId: Int): List<TrainingTemplate>

    fun addTrainingTemplate(userId: Int, trainingTemplate: CreateTrainingTemplate): TrainingTemplate

    fun updateTrainingTemplate(userId: Int, trainingTemplate: CreateTrainingTemplate): TrainingTemplate

    fun removeTrainingTemplate(userId: Int, trainingTemplateId: Int): TrainingTemplate

    fun createTraining(userId: Int, templateId: Int): Training

    fun getTraining(userId: Int, trainingId: Int): Training

    fun getTrainings(userId: Int): List<Training>

    fun removeTraining(userId: Int, trainingId: Int): Training

    fun updateTraining(userId: Int, trainingId: Int, training: CreateTraining): Training
}

class TrainingServiceImpl(
        private val templateRepository: TrainingTemplateRepository,
        private val trainingRepository: TrainingRepository,
        private val exercisesRepository: ExercisesRepository,
        private val categoriesRepository: CategoriesRepository,
        private val equipmentRepository: EquipmentRepository,
        private val timeProvider: TimeProvider
) : TrainingService {
    override fun getTrainingTemplates(userId: Int): List<TrainingTemplate> = templateRepository.getTemplates(userId)

    override fun addTrainingTemplate(userId: Int, trainingTemplate: CreateTrainingTemplate): TrainingTemplate =
            templateRepository.addTemplate(userId, trainingTemplate)

    override fun updateTrainingTemplate(userId: Int, trainingTemplate: CreateTrainingTemplate): TrainingTemplate =
            templateRepository.updateTemplate(userId, trainingTemplate)

    override fun removeTrainingTemplate(userId: Int, trainingTemplateId: Int): TrainingTemplate =
            templateRepository.removeTemplate(userId, trainingTemplateId)

    override fun createTraining(userId: Int, templateId: Int): Training {
        val newTraining = trainingRepository.createTraining(userId, templateId)
        val template = templateRepository.getTemplate(userId, templateId)
        val allExercises = exercisesRepository.getAll().filter { exercise -> exercise.neededEquipment.all { equipment -> equipment in template.availableEquipment } }

        val trainingUpdate = CreateTraining(
                timeProvider.currentTime(),
                exercisesWithProgress = template.exercises.map { trainingPart ->
                    val exercise = allExercises.filter { exercise ->
                        exercise.category.all { it in trainingPart.categories } &&
                                exercise.id !in trainingPart.excludedExercises &&
                                exercise.neededEquipment.none { it in trainingPart.excludedEquipment }
                    }.randomOrNull()
                            ?: trainingPart.pinnedExercise
                            ?: throw NoSuchElementException("No exercise for training part with: ${trainingPart.id}")
                    CreateTrainingExerciseWithProgress(exercise.id, emptyList())
                }
        )
        return trainingRepository.updateTraining(
                userId = userId,
                trainingId = newTraining.id,
                training = trainingUpdate
        )
    }

    override fun getTraining(userId: Int, trainingId: Int): Training =
            trainingRepository.getTraining(userId, trainingId)

    override fun getTrainings(userId: Int): List<Training> = trainingRepository.getTrainings(userId)

    override fun removeTraining(userId: Int, trainingId: Int): Training =
            trainingRepository.removeTrainings(userId, trainingId)

    override fun updateTraining(userId: Int, trainingId: Int, training: CreateTraining): Training {
        return trainingRepository.updateTraining(userId, trainingId, training)
    }
}