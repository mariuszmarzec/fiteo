package com.marzec.fiteo.repositories

import com.marzec.database.*
import com.marzec.fiteo.model.domain.UpdateTraining
import com.marzec.fiteo.model.domain.UpdateTrainingExerciseWithProgress
import com.marzec.fiteo.model.domain.Series
import com.marzec.fiteo.model.domain.Training
import kotlinx.datetime.toJavaLocalDateTime
import org.jetbrains.exposed.sql.*

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

    override fun updateTraining(userId: Int, trainingId: Int, training: UpdateTraining): Training = database.dbCall {
        val userEntity = database.dbCall { UserEntity.findByIdOrThrow(userId) }

        val trainingEntity = database.dbCall {
            TrainingEntity.findByIdOrThrow(trainingId).apply {
                belongsToUserOrThrow(userId)
            }
        }

        val newExercises = createOrUpdateTrainingExercises(trainingEntity, training, userEntity)
        removePartsIfNotPresentInNewOnes(newExercises, trainingEntity)

        trainingEntity.exercises = newExercises.toSized()
        trainingEntity.finishDateInMillis = training.finishDateInMillis.toJavaLocalDateTime()

        trainingEntity.toDomain()
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
        trainingEntity: TrainingEntity,
        training: UpdateTraining,
        userEntity: UserEntity
    ) = training.exercisesWithProgress.mapIndexed { index, exercise ->
        exercise.id?.let { id ->
            TrainingExerciseWithProgressEntity.findById(id)?.apply {
                if (this.training.first().id.value != trainingEntity.id.value) {
                    throw IllegalAccessException("Part with $id belongs to different training")
                }
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
                name = createPart.name
                this.ordinalNumber = ordinalNumber
                exercise = ExerciseEntity.findByIdOrThrow(createPart.exerciseId)
            }
        }
        return progressEntity.apply { updateTraining(ordinalNumber, userEntity, createPart) }
    }

    private fun TrainingExerciseWithProgressEntity.updateTraining(
        ordinalNumber: Int,
        userEntity: UserEntity,
        createPart: UpdateTrainingExerciseWithProgress
    ) {
        val newSeries = createSeries(createPart, userEntity)

        exercise = ExerciseEntity.findByIdOrThrow(createPart.exerciseId)
        templatePartId = createPart.trainingPartId
        name = createPart.name
        this.ordinalNumber = ordinalNumber

        removeSeriesIfNotPresentInNewOnes(newSeries, series)
        series = newSeries.toSized()
    }

    private fun createSeries(
        createPart: UpdateTrainingExerciseWithProgress,
        userEntity: UserEntity
    ) = createPart.series.mapIndexed { index, series ->
        SeriesEntity.findById(series.seriesId)?.apply {
            updateSeriesEntity(series, index, userEntity)
        } ?: createSeries(index, userEntity, series)
    }

    private fun createSeries(ordinalNumber: Int, userEntity: UserEntity, series: Series): SeriesEntity =
        database.dbCall {
            SeriesEntity.new {
                updateSeriesEntity(series, ordinalNumber, userEntity)
            }
        }

    private fun removeSeriesIfNotPresentInNewOnes(
        newSeries: List<SeriesEntity>,
        oldSeries: SizedIterable<SeriesEntity>
    ) {
        val newPartsIds = newSeries.map { it.id.value }
        val seriesToRemove = oldSeries.filterNot { it.id.value in newPartsIds }
        seriesToRemove.forEach { it.delete() }
    }

    private fun SeriesEntity.updateSeriesEntity(
        series: Series,
        ordinalNumber: Int,
        userEntity: UserEntity
    ) {
        date = series.date.toJavaLocalDateTime()
        burden = series.burden
        timeInMillis = series.timeInMillis
        repsNumber = series.repsNumber
        note = series.note
        this.ordinalNumber = ordinalNumber
        user = userEntity
    }
}
