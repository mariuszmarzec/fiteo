package com.marzec.exercises

import com.marzec.model.domain.CreateTraining
import com.marzec.model.domain.CreateTrainingTemplate
import com.marzec.model.domain.Training
import com.marzec.model.domain.TrainingTemplate
import com.marzec.repositories.TrainingRepository
import com.marzec.repositories.TrainingTemplateRepository

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
        private val trainingRepository: TrainingRepository
) : TrainingService {
    override fun getTrainingTemplates(userId: Int): List<TrainingTemplate> = templateRepository.getTemplates(userId)

    override fun addTrainingTemplate(userId: Int, trainingTemplate: CreateTrainingTemplate): TrainingTemplate =
            templateRepository.addTemplate(userId, trainingTemplate)

    override fun updateTrainingTemplate(userId: Int, trainingTemplate: CreateTrainingTemplate): TrainingTemplate =
            templateRepository.updateTemplate(userId, trainingTemplate)

    override fun removeTrainingTemplate(userId: Int, trainingTemplateId: Int): TrainingTemplate =
            templateRepository.removeTemplate(userId, trainingTemplateId)

    override fun createTraining(userId: Int, templateId: Int): Training =
            trainingRepository.createTraining(userId, templateId)

    override fun getTraining(userId: Int, trainingId: Int): Training =
            trainingRepository.getTraining(userId, trainingId)

    override fun getTrainings(userId: Int): List<Training> = trainingRepository.getTrainings(userId)

    override fun removeTraining(userId: Int, trainingId: Int): Training =
            trainingRepository.removeTrainings(userId, trainingId)

    override fun updateTraining(userId: Int, trainingId: Int, training: CreateTraining): Training {
        return trainingRepository.updateTraining(userId, trainingId, training)
    }
}