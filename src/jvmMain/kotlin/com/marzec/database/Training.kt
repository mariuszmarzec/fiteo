package com.marzec.database

import com.marzec.core.currentTime
import com.marzec.fiteo.model.domain.Series
import com.marzec.fiteo.model.domain.Training
import com.marzec.fiteo.model.domain.TrainingExerciseWithProgress
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.`java-time`.datetime

object TrainingsTable : IntIdTable("trainings") {
    val templateId = reference("template_id", TrainingTemplateTable, onDelete = ReferenceOption.NO_ACTION)
    val createDateInMillis = datetime("create_date_in_millis").apply { defaultValueFun = { currentTime() } }
    val finishDateInMillis = datetime("finish_date_in_millis").apply { defaultValueFun = { currentTime() } }
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
            exercisesWithProgress = exercises.map { it.toDomain(id.value) },
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
}

class TrainingExerciseWithProgressEntity(id: EntityID<Int>) : IntEntityWithUser(id) {
    var exercise by ExerciseEntity referencedOn TrainingExerciseWithProgressTable.exerciseId
    var series by SeriesEntity via ExerciseToSeries
    override var user by UserEntity referencedOn TrainingExerciseWithProgressTable.userId

    fun toDomain(
            trainingId: Int
    ) = TrainingExerciseWithProgress(
            exercise = exercise.toDomain(),
            series = series.map { it.toDomain(exerciseId = exercise.id.value, trainingId = trainingId) }
    )

    companion object : IntEntityClass<TrainingExerciseWithProgressEntity>(TrainingExerciseWithProgressTable)
}

object ExerciseToSeries : IntIdTable("exercise_to_series") {
    val exerciseId = reference("exercise_id", TrainingExerciseWithProgressTable, onDelete = ReferenceOption.CASCADE)
    val seriesId = reference("series_id", SeriesTable, onDelete = ReferenceOption.CASCADE)
}

object SeriesTable : IntIdTable("series") {
    val date = datetime("date").apply { defaultValueFun = { currentTime() } }
    val burden = integer("burden").nullable()
    val timeInMillis = long("time_in_millis").nullable()
    val repsNumber = integer("reps_number").nullable()
    val note = text("note")
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
}

class SeriesEntity(id: EntityID<Int>) : IntEntityWithUser(id) {
    var date by SeriesTable.date
    var burden by SeriesTable.burden
    var timeInMillis by SeriesTable.timeInMillis
    var repsNumber by SeriesTable.repsNumber
    var note by SeriesTable.note

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
