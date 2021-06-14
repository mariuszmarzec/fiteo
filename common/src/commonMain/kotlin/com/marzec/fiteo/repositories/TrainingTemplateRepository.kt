package com.marzec.fiteo.repositories

import com.marzec.model.domain.CreateTrainingTemplate
import com.marzec.model.domain.TrainingTemplate

interface TrainingTemplateRepository {

    fun getTemplates(userId: Int): List<TrainingTemplate>

    fun getTemplate(userId: Int, templateId: Int): TrainingTemplate
    
    fun addTemplate(userId: Int, trainingTemplate: CreateTrainingTemplate): TrainingTemplate
    
    fun updateTemplate(userId: Int, trainingTemplate: CreateTrainingTemplate): TrainingTemplate
    
    fun removeTemplate(userId: Int, trainingTemplateId: Int): TrainingTemplate
}