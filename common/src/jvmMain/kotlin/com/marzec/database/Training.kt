package com.marzec.database

import java.time.LocalDateTime
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.`java-time`.datetime

object TrainingsTable : IntIdTable("trainings") {
    val templateId = reference("template_id", TrainingTemplateTable, onDelete = ReferenceOption.NO_ACTION)
    val createDateInMillis = datetime("create_date_in_millis").apply { defaultValueFun = { LocalDateTime.now()} }
    val finishDateInMillis = datetime("finish_date_in_millis").apply { defaultValueFun = { LocalDateTime.now()} }
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
}

class TrainingEntity(id: EntityID<Int>) : IntEntityWithUser(id) {
    var templateId by TrainingsTable.templateId
    var createDateInMillis by TrainingsTable.createDateInMillis
    var finishDateInMillis by TrainingsTable.finishDateInMillis
    var exercises by TrainingExerciseWithProgressEntity via TrainingToExercisesTable
    override var user by UserEntity referencedOn TrainingsTable.userId

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
    val exercise by ExerciseEntity referencedOn TrainingExerciseWithProgressTable.exerciseId
    var series by SeriesEntity via ExerciseToSeries
    override var user by UserEntity referencedOn TrainingExerciseWithProgressTable.userId

    companion object : IntEntityClass<TrainingExerciseWithProgressEntity>(TrainingExerciseWithProgressTable)
}

object ExerciseToSeries : IntIdTable("exercise_to_series") {
    val exerciseId = reference("exercise_id", TrainingExerciseWithProgressTable, onDelete = ReferenceOption.CASCADE)
    val seriesId = reference("series_id", SeriesTable, onDelete = ReferenceOption.CASCADE)
}

object SeriesTable : IntIdTable("series") {
    val date = datetime("date").apply { defaultValueFun = { LocalDateTime.now()} }
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
    override var user by UserEntity referencedOn SeriesTable.userId

    companion object : IntEntityClass<SeriesEntity>(SeriesTable)
}