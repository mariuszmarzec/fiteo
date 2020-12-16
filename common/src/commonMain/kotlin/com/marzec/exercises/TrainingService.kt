package com.marzec.exercises

import com.marzec.model.domain.CreateTrainingTemplate
import com.marzec.model.domain.TrainingTemplate
import com.marzec.repositories.TrainingTemplateRepository

interface TrainingService {
    fun getTrainingTemplates(userId: Int): List<TrainingTemplate>

    fun addTrainingTemplate(userId: Int, trainingTemplate: CreateTrainingTemplate): TrainingTemplate

    fun updateTrainingTemplate(userId: Int, trainingTemplate: CreateTrainingTemplate): TrainingTemplate

    fun removeTrainingTemplate(userId: Int, trainingTemplateId: Int): TrainingTemplate
}

class TrainingServiceImpl(
        private val repository: TrainingTemplateRepository
) : TrainingService {
    override fun getTrainingTemplates(userId: Int): List<TrainingTemplate> = repository.getTemplates(userId)

    override fun addTrainingTemplate(userId: Int, trainingTemplate: CreateTrainingTemplate): TrainingTemplate =
            repository.addTemplate(userId, trainingTemplate)

    override fun updateTrainingTemplate(userId: Int, trainingTemplate: CreateTrainingTemplate): TrainingTemplate =
            repository.updateTemplate(userId, trainingTemplate)

    override fun removeTrainingTemplate(userId: Int, trainingTemplateId: Int): TrainingTemplate =
            repository.removeTemplate(userId, trainingTemplateId)
}