package com.marzec.exercises

import com.marzec.model.domain.CreateTraining
import com.marzec.model.domain.CreateTrainingTemplate
import com.marzec.model.domain.Training
import com.marzec.model.domain.TrainingTemplate
import com.marzec.repositories.TrainingRepository
import com.marzec.repositories.TrainingTemplateRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class TrainingServiceImplTest {

    private val templateRepository = mockk
        private val trainingRepository = object : TrainingRepository {
        override fun createTraining(userId: Int, templateId: Int): Training {
            TODO("Not yet implemented")
        }

        override fun getTraining(userId: Int, trainingId: Int): Training {
            TODO("Not yet implemented")
        }

        override fun getTrainings(userId: Int): List<Training> {
            TODO("Not yet implemented")
        }

        override fun removeTrainings(userId: Int, trainingId: Int): Training {
            TODO("Not yet implemented")
        }

        override fun updateTraining(userId: Int, trainingId: Int, training: CreateTraining): Training {
            TODO("Not yet implemented")
        }

    }

    val trainingService = TrainingServiceImpl(templateRepository, trainingRepository)
}