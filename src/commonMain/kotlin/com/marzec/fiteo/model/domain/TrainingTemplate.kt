package com.marzec.fiteo.model.domain

import com.marzec.fiteo.model.dto.CategoryDto
import com.marzec.fiteo.model.dto.EquipmentDto
import com.marzec.fiteo.model.dto.ExerciseDto
import com.marzec.fiteo.model.dto.toDomain
import kotlinx.serialization.Serializable

data class TrainingTemplate(
    val id: Int,
    val name: String,
    val exercises: List<TrainingTemplatePart>,
    val availableEquipment: List<Equipment>
)

data class TrainingTemplatePart(
    val id: Int,
    val name: String,
    val pinnedExercise: Exercise?,
    val categories: List<Category>,
    val excludedExercises: List<Int>,
    val excludedEquipment: List<Equipment>
)

@Serializable
data class TrainingTemplateDto(
    val id: Int,
    val name: String,
    val exercises: List<TrainingTemplatePartDto>,
    val availableEquipment: List<EquipmentDto>
)

@Serializable
data class TrainingTemplatePartDto(
    val id: Int,
    val name: String,
    val pinnedExercise: ExerciseDto?,
    val categories: List<CategoryDto>,
    val excludedExercises: List<Int>,
    val excludedEquipment: List<EquipmentDto>
)

fun TrainingTemplate.toDto() = TrainingTemplateDto(
    id = id,
    name = name,
    exercises = exercises.map { it.toDto() },
    availableEquipment = availableEquipment.map { it.toDto() }
)

fun TrainingTemplatePart.toDto() = TrainingTemplatePartDto(
    id = id,
    name = name,
    pinnedExercise = pinnedExercise?.toDto(),
    categories = categories.map { it.toDto() },
    excludedExercises = excludedExercises,
    excludedEquipment = excludedEquipment.map { it.toDto() }
)

fun TrainingTemplateDto.toDomain() = TrainingTemplate(
    id = id,
    name = name,
    exercises = exercises.map { it.toDomain() },
    availableEquipment = availableEquipment.map { it.toDomain() }
)

fun TrainingTemplatePartDto.toDomain() = TrainingTemplatePart(
    id = id,
    name = name,
    pinnedExercise = pinnedExercise?.toDomain(),
    categories = categories.map { it.toDomain() },
    excludedExercises = excludedExercises,
    excludedEquipment = excludedEquipment.map { it.toDomain() }
)

data class CreateTrainingTemplate(
    val id: Int,
    val name: String,
    val exercises: List<CreateTrainingTemplatePart>,
    val availableEquipmentIds: List<String>
)

data class CreateTrainingTemplatePart(
    val id: Int? = null,
    val name: String,
    val pinnedExerciseId: Int?,
    val categoryIds: List<String>,
    val excludedExercisesIds: List<Int>,
    val excludedEquipmentIds: List<String>
)

@Serializable
data class CreateTrainingTemplateDto(
    val id: Int,
    val name: String,
    val exercises: List<CreateTrainingTemplatePartDto>,
    val availableEquipmentIds: List<String>
)

@Serializable
data class CreateTrainingTemplatePartDto(
    val id: Int? = null,
    val name: String,
    val pinnedExerciseId: Int? = null,
    val categoryIds: List<String>,
    val excludedExercisesIds: List<Int>,
    val excludedEquipmentIds: List<String>
)

fun CreateTrainingTemplateDto.toDomain() = CreateTrainingTemplate(
    id = id,
    name = name,
    exercises = exercises.map { it.toDomain() },
    availableEquipmentIds = availableEquipmentIds
)

fun CreateTrainingTemplatePartDto.toDomain() = CreateTrainingTemplatePart(
    id = id,
    name = name,
    pinnedExerciseId = pinnedExerciseId,
    categoryIds = categoryIds,
    excludedExercisesIds = excludedExercisesIds,
    excludedEquipmentIds = excludedEquipmentIds
)
