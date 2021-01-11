package com.marzec.repositories

import com.marzec.database.ExerciseEntity
import com.marzec.database.SeriesEntity
import com.marzec.database.TrainingEntity
import com.marzec.database.TrainingExerciseWithProgressEntity
import com.marzec.database.TrainingTemplateEntity
import com.marzec.database.TrainingsTable
import com.marzec.database.UserEntity
import com.marzec.database.dbCall
import com.marzec.database.findByIdOrThrow
import com.marzec.database.toSized
import com.marzec.model.domain.CreateTraining
import com.marzec.model.domain.CreateTrainingExerciseWithProgress
import com.marzec.model.domain.Series
import com.marzec.model.domain.Training
import kotlinx.datetime.toJavaLocalDateTime
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll

class TrainingRepositoryImpl : TrainingRepository {

    override fun createTraining(userId: Int, templateId: Int): Training {
        val userEntity = dbCall { UserEntity.findByIdOrThrow(userId) }
        val trainingTemplateEntity = dbCall { TrainingTemplateEntity.findByIdOrThrow(templateId) }
        val trainingEntity = dbCall {
            TrainingEntity.new {
                template = trainingTemplateEntity
                user = userEntity
            }
        }
        return dbCall { trainingEntity.toDomain() }
    }

    override fun getTraining(userId: Int, trainingId: Int): Training {
        return dbCall {
            val trainingEntity = TrainingEntity.findByIdOrThrow(trainingId)
            trainingEntity.belongsToUserOrThrow(userId)
            trainingEntity.toDomain()
        }
    }

    override fun getTrainings(userId: Int): List<Training> {
        return dbCall {
            TrainingsTable.selectAll().andWhere { TrainingsTable.userId eq userId }
                    .map { TrainingEntity.wrapRow(it).toDomain() }
        }
    }

    override fun removeTrainings(userId: Int, trainingId: Int): Training {
        return dbCall {
            val trainingEntity = TrainingEntity.findByIdOrThrow(trainingId)
            trainingEntity.deleteIfBelongsToUserOrThrow(userId)
            trainingEntity.toDomain()
        }
    }

    override fun updateTraining(userId: Int, trainingId: Int, training: CreateTraining): Training {
        val userEntity = dbCall { UserEntity.findByIdOrThrow(userId) }

        val trainingEntity = dbCall {
            TrainingEntity.findByIdOrThrow(trainingId).apply {
                belongsToUserOrThrow(userId)
            }
        }

        val exercises = training.exercisesWithProgress.map {
            createTrainingWithExercises(userEntity, it)
        }


        return dbCall {
            trainingEntity.exercises = exercises.toSized()
            trainingEntity.toDomain()
        }
    }

    private fun createTrainingWithExercises(userEntity: UserEntity, training: CreateTrainingExerciseWithProgress): TrainingExerciseWithProgressEntity {
        val series = training.series.map {
            createSeries(userEntity, it)
        }

        return dbCall {
            TrainingExerciseWithProgressEntity.new {
                this.exercise = ExerciseEntity.findByIdOrThrow(training.exerciseId)
                this.series = series.toSized()
            }
        }
    }

    private fun createSeries(userEntity: UserEntity, series: Series): SeriesEntity {
        return dbCall {
            SeriesEntity.new {
                date = series.date.toJavaLocalDateTime()
                burden = series.burden
                timeInMillis = series.timeInMillis
                repsNumber = series.repsNumber
                note = series.note
                user = userEntity
            }
        }
    }
}