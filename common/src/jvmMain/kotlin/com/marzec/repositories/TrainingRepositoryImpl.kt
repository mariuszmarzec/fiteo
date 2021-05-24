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
import java.time.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll

class TrainingRepositoryImpl(private val database: Database) : TrainingRepository {

    override fun createTraining(userId: Int, templateId: Int): Training {
        val userEntity = database.dbCall { UserEntity.findByIdOrThrow(userId) }
        val trainingTemplateEntity = database.dbCall { TrainingTemplateEntity.findByIdOrThrow(templateId) }
        val trainingEntity = database.dbCall {
            TrainingEntity.new {
                template = trainingTemplateEntity
                user = userEntity
            }
        }
        return database.dbCall { trainingEntity.toDomain() }
    }

    override fun getTraining(userId: Int, trainingId: Int): Training {
        return database.dbCall {
            val trainingEntity = TrainingEntity.findByIdOrThrow(trainingId)
            trainingEntity.belongsToUserOrThrow(userId)
            trainingEntity.toDomain()
        }
    }

    override fun getTrainings(userId: Int): List<Training> {
        return database.dbCall {
            TrainingsTable.selectAll().andWhere { TrainingsTable.userId eq userId }
                .map { TrainingEntity.wrapRow(it).toDomain() }
        }
    }

    override fun removeTrainings(userId: Int, trainingId: Int): Training {
        return database.dbCall {
            val trainingEntity = TrainingEntity.findByIdOrThrow(trainingId)
            trainingEntity.deleteIfBelongsToUserOrThrow(userId)
            trainingEntity.toDomain()
        }
    }

    override fun updateTraining(userId: Int, trainingId: Int, training: CreateTraining): Training {
        val userEntity = database.dbCall { UserEntity.findByIdOrThrow(userId) }

        val trainingEntity = database.dbCall {
            TrainingEntity.findByIdOrThrow(trainingId).apply {
                belongsToUserOrThrow(userId)
            }
        }

        val exercises = training.exercisesWithProgress.map {
            createTrainingWithExercises(userEntity, it)
        }


        return database.dbCall {
            trainingEntity.exercises = exercises.toSized()
            trainingEntity.finishDateInMillis = training.finishDateInMillis.toJavaLocalDateTime()
            trainingEntity.toDomain()
        }
    }

    private fun createTrainingWithExercises(
        userEntity: UserEntity,
        training: CreateTrainingExerciseWithProgress
    ): TrainingExerciseWithProgressEntity {
        val series = training.series.map {
            createSeries(userEntity, it)
        }

        return database.dbCall {
            TrainingExerciseWithProgressEntity.new {
                this.user = userEntity
                this.exercise = ExerciseEntity.findByIdOrThrow(training.exerciseId)
                if (series.isNotEmpty()) {
                    this.series = series.toSized()
                }
            }
        }
    }

    private fun createSeries(userEntity: UserEntity, series: Series): SeriesEntity {
        return database.dbCall {
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