package com.marzec.repositories

import com.marzec.model.domain.CreateTrainingTemplate
import com.marzec.model.domain.TrainingTemplate

class TrainingTemplateRepositoryImpl : TrainingTemplateRepository {

    override fun getTemplates(userId: Int): List<TrainingTemplate> {
        TODO("Not yet implemented")
    }

    override fun addTemplate(userId: Int, trainingTemplate: CreateTrainingTemplate): TrainingTemplate {
        TODO("Not yet implemented")
    }

    override fun updateTemplate(userId: Int, trainingTemplate: CreateTrainingTemplate): TrainingTemplate {
        TODO("Not yet implemented")
    }

    override fun removeTemplate(userId: Int, trainingTemplateId: Int): TrainingTemplate {
        TODO("Not yet implemented")
    }
}