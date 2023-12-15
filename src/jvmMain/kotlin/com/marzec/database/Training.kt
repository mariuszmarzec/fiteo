package com.marzec.database

import com.marzec.core.currentTime
import com.marzec.fiteo.model.domain.Series
import com.marzec.fiteo.model.domain.Training
import com.marzec.fiteo.model.domain.TrainingExerciseWithProgress
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.javatime.datetime

object TrainingsTable : IntIdTable("trainings") {
    val templateId = reference("template_id", TrainingTemplateTable, onDelete = ReferenceOption.NO_ACTION)
    val createDateInMillis = datetime("create_date_in_millis").apply {
        defaultValueFun = { currentTime().toJavaLocalDateTime() }
    }
    val finishDateInMillis = datetime("finish_date_in_millis").apply {
        defaultValueFun = { currentTime().toJavaLocalDateTime() }
    }
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
}

class TrainingEntity(id: EntityID<Int>) : IntEntityWithUser(id) {

    var template by TrainingTemplateEntity referencedOn TrainingsTable.templateId
    var createDateInMillis by TrainingsTable.createDateInMillis
    var finishDateInMillis by TrainingsTable.finishDateInMillis
    var exercises by TrainingExerciseWithProgressEntity via TrainingToExercisesTable
    override var user by UserEntity referencedOn TrainingsTable.userId

    fun toDomain() = Training(
        id = id.value,
        templateId = template.id.value,
        createDateInMillis = createDateInMillis.toKotlinLocalDateTime(),
        finishDateInMillis = finishDateInMillis.toKotlinLocalDateTime(),
        exercisesWithProgress = exercises
            .sortedBy { it.ordinalNumber }
            .map { it.toDomain(id.value) },
    )

    companion object : IntEntityClass<TrainingEntity>(TrainingsTable)
}

object TrainingToExercisesTable : IntIdTable("training_to_exercise") {
    val trainingId = reference("training_id", TrainingsTable, onDelete = ReferenceOption.CASCADE)
    val exerciseId = reference("exercise_id", TrainingExerciseWithProgressTable, onDelete = ReferenceOption.CASCADE)
}

object TrainingExerciseWithProgressTable : IntIdTable("exercise_with_progress") {
    val exerciseId = reference("exercise_id", ExerciseTable, onDelete = ReferenceOption.NO_ACTION)
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val templateId = integer("template_part_id").nullable()
    val name = varchar("name", 255)
    val ordinalNumber = integer("ordinal_number")
}

class TrainingExerciseWithProgressEntity(id: EntityID<Int>) : IntEntityWithUser(id) {
    var exercise by ExerciseEntity referencedOn TrainingExerciseWithProgressTable.exerciseId
    var series by SeriesEntity via ExerciseToSeries
    var templatePartId by TrainingExerciseWithProgressTable.templateId
    var name by TrainingExerciseWithProgressTable.name
    var ordinalNumber by TrainingExerciseWithProgressTable.ordinalNumber
    override var user by UserEntity referencedOn TrainingExerciseWithProgressTable.userId

    val training by TrainingEntity via TrainingToExercisesTable

    fun toDomain(
        trainingId: Int
    ) = TrainingExerciseWithProgress(
        id = id.value,
        exercise = exercise.toDomain(),
        series = series
            .sortedBy { it.ordinalNumber }
            .map { it.toDomain(exerciseId = exercise.id.value, trainingId = trainingId) },
        templatePart = templatePartId?.let { TrainingTemplatePartEntity.findById(it) }?.toDomain(),
        name = name
    )

    companion object : IntEntityClass<TrainingExerciseWithProgressEntity>(TrainingExerciseWithProgressTable)
}

object ExerciseToSeries : IntIdTable("exercise_to_series") {
    val exerciseId = reference("exercise_id", TrainingExerciseWithProgressTable, onDelete = ReferenceOption.CASCADE)
    val seriesId = reference("series_id", SeriesTable, onDelete = ReferenceOption.CASCADE)
}

object SeriesTable : IntIdTable("series") {
    val date = datetime("date").apply { defaultValueFun = { currentTime().toJavaLocalDateTime() } }
    val burden = float("burden").nullable()
    val timeInMillis = long("time_in_millis").nullable()
    val repsNumber = integer("reps_number").nullable()
    val note = text("note")
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val ordinalNumber = integer("ordinal_number")
}

class SeriesEntity(id: EntityID<Int>) : IntEntityWithUser(id) {
    var date by SeriesTable.date
    var burden by SeriesTable.burden
    var timeInMillis by SeriesTable.timeInMillis
    var repsNumber by SeriesTable.repsNumber
    var note by SeriesTable.note
    var ordinalNumber by SeriesTable.ordinalNumber

    override var user by UserEntity referencedOn SeriesTable.userId

    fun toDomain(
        exerciseId: Int,
        trainingId: Int
    ) = Series(
        seriesId = id.value,
        exerciseId = exerciseId,
        trainingId = trainingId,
        date = date.toKotlinLocalDateTime(),
        burden = burden,
        timeInMillis = timeInMillis,
        repsNumber = repsNumber,
        note = note
    )

    companion object : IntEntityClass<SeriesEntity>(SeriesTable)
}
