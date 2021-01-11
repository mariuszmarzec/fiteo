package com.marzec.repositories

import com.marzec.model.domain.CreateTraining
import com.marzec.model.domain.Training

interface TrainingRepository {

    fun createTraining(userId: Int, templateId: Int): Training

    fun getTraining(userId: Int, trainingId: Int): Training

    fun getTrainings(userId: Int): List<Training>

    fun removeTrainings(userId: Int, trainingId: Int): Training

    fun updateTraining(userId: Int, trainingId: Int, training: CreateTraining): Training
}