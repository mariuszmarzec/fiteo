package com.marzec.fiteo.repositories

import com.marzec.database.*
import com.marzec.fiteo.model.domain.UpdateTraining
import com.marzec.fiteo.model.domain.UpdateTrainingExerciseWithProgress
import com.marzec.fiteo.model.domain.Series
import com.marzec.fiteo.model.domain.Training
import kotlinx.datetime.toJavaLocalDateTime
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll

class TrainingRepositoryImpl(private val database: Database) : TrainingRepository {

    override fun createTraining(userId: Int, templateId: Int): Training = database.dbCall {
        val userEntity = UserEntity.findByIdOrThrow(userId)
        val trainingTemplateEntity = TrainingTemplateEntity.findByIdOrThrow(templateId)
        val trainingEntity = TrainingEntity.new {
            template = trainingTemplateEntity
            user = userEntity
        }
        trainingEntity.toDomain()
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
            TrainingsTable.selectAll()
                .andWhere { TrainingsTable.userId eq userId }
                .orderBy(TrainingsTable.createDateInMillis, SortOrder.DESC)
                .map { TrainingEntity.wrapRow(it).toDomain() }
        }
    }

    override fun removeTrainings(userId: Int, trainingId: Int): Training {
        return database.dbCall {
            val trainingEntity = TrainingEntity.findByIdOrThrow(trainingId)
            val domain = trainingEntity.toDomain()
            trainingEntity.deleteIfBelongsToUserOrThrow(userId)
            domain
        }
    }

    override fun updateTraining(userId: Int, trainingId: Int, training: UpdateTraining): Training {
        val userEntity = database.dbCall { UserEntity.findByIdOrThrow(userId) }

        val trainingEntity = database.dbCall {
            TrainingEntity.findByIdOrThrow(trainingId).apply {
                belongsToUserOrThrow(userId)
            }
        }

        val newExercises = createOrUpdateTrainingExercises(training, userEntity)
        removePartsIfNotPresentInNewOnes(newExercises, trainingEntity)

        database.dbCall {
            trainingEntity.exercises = newExercises.toSized()
            trainingEntity.finishDateInMillis = training.finishDateInMillis.toJavaLocalDateTime()
        }

        return database.dbCall {
            trainingEntity.toDomain()
        }
    }

    private fun removePartsIfNotPresentInNewOnes(
        newExercises: List<TrainingExerciseWithProgressEntity>,
        trainingEntity: TrainingEntity
    ) {
        val newPartsIds = newExercises.map { it.id.value }
        val partsToRemove = trainingEntity.exercises.filterNot { it.id.value in newPartsIds }
        partsToRemove.forEach { it.delete() }
    }

    private fun createOrUpdateTrainingExercises(
        training: UpdateTraining,
        userEntity: UserEntity
    ) = training.exercisesWithProgress.mapIndexed { index, exercise ->
        exercise.id?.let {
            TrainingExerciseWithProgressEntity.findById(it)?.apply {
                updateTraining(index, userEntity, exercise)
            }
        } ?: createTrainingWithExercises(index, userEntity, exercise)
    }

    private fun createTrainingWithExercises(
        ordinalNumber: Int,
        userEntity: UserEntity,
        createPart: UpdateTrainingExerciseWithProgress
    ): TrainingExerciseWithProgressEntity {
        val progressEntity = database.dbCall {
            TrainingExerciseWithProgressEntity.new {
                this.user = userEntity
            }
        }
        return progressEntity.apply { updateTraining(ordinalNumber, userEntity, createPart) }
    }

    private fun TrainingExerciseWithProgressEntity.updateTraining(
        ordinalNumber: Int,
        userEntity: UserEntity,
        createPart: UpdateTrainingExerciseWithProgress
    ) {
        val newSeries = createPart.series.map {
            createSeries(userEntity, it)
        }

        exercise = ExerciseEntity.findByIdOrThrow(createPart.exerciseId)
        templatePartId = createPart.trainingPartId
        name = createPart.name
        this.ordinalNumber = ordinalNumber

        series = newSeries.toSized()
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
