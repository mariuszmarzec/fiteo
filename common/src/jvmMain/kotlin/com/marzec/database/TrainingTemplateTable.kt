package com.marzec.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object TrainingTemplateTable : IntIdTable("training_templates") {
    val name = varchar("name", 300)
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
}

class TrainingTemplateEntity(id: EntityID<Int>) : IntEntity(id) {
    var name by TrainingTemplateTable.name
    var user by UserEntity referencedOn TrainingTemplateTable.userId
    var trainingTemplatePart by TrainingTemplatePartEntity via TrainingTemplateToTrainingTemplatePartTable
    var availableEquipment by EquipmentEntity via TrainingTemplateToAvailableEquipmentTable
}

object TrainingTemplateToAvailableEquipmentTable : IntIdTable("training_templates_to_available_equipment") {
    val trainingTemplateId = reference("training_template_id", TrainingTemplateTable, onDelete = ReferenceOption.CASCADE)
    val availableEquipmentId = reference("available_equipment_id", EquipmentTable, onDelete = ReferenceOption.CASCADE)
}

object TrainingTemplateToTrainingTemplatePartTable : IntIdTable("training_templates_to_training_part") {
    val trainingTemplateId = reference("training_template_id", TrainingTemplateTable, onDelete = ReferenceOption.CASCADE)
    val trainingTemplatePartId = reference("training_part_id", TrainingTemplatePartTable, onDelete = ReferenceOption.CASCADE)
}

object TrainingTemplatePartTable : IntIdTable("training_parts") {
    val name = varchar("name", 300)
}

class TrainingTemplatePartEntity(id: EntityID<Int>) : IntEntity(id) {
    var name by TrainingTemplatePartTable.name
    var categories by CategoryEntity via TrainingTemplatePartToCategoriesTable
    var excludedExercises by ExerciseEntity via TrainingTemplatePartToExcludedExercisesTable
    var excludedEquipment by EquipmentEntity via TrainingTemplatePartToExcludedEquipmentTable

    companion object : IntEntityClass<TrainingTemplatePartEntity>(TrainingTemplatePartTable)
}

object TrainingTemplatePartToCategoriesTable : IntIdTable("training_parts_to_categories") {
    val trainingTemplatePartId = reference("training_part_id", TrainingTemplateTable, onDelete = ReferenceOption.CASCADE)
    val categoryId = reference("category_id", CategoryTable, onDelete = ReferenceOption.CASCADE)
}

object TrainingTemplatePartToExcludedExercisesTable : IntIdTable("training_parts_to_excluded_exercises") {
    val trainingTemplatePartId = reference("training_part_id", TrainingTemplateTable, onDelete = ReferenceOption.CASCADE)
    val excludedExerciseId = reference("excluded_exercise_id", ExerciseTable, onDelete = ReferenceOption.CASCADE)
}

object TrainingTemplatePartToExcludedEquipmentTable : IntIdTable("training_parts_to_excluded_equipment") {
    val trainingTemplatePartId = reference("training_part_id", TrainingTemplateTable, onDelete = ReferenceOption.CASCADE)
    val excludedEquipmentId = reference("excluded_equipment", EquipmentTable, onDelete = ReferenceOption.CASCADE)
}