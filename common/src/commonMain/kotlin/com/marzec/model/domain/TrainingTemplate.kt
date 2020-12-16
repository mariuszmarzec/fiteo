package com.marzec.model.domain

import com.marzec.model.dto.CategoryDto
import com.marzec.model.dto.EquipmentDto
import com.marzec.model.dto.ExerciseDto
import com.marzec.model.dto.toDomain

data class TrainingTemplate(
        val id: String,
        val name: String,
        val exercises: List<TrainingTemplatePart>,
        val availableEquipment: List<Equipment>
)

data class TrainingTemplatePart(
        val id: String,
        val name: String,
        val category: List<Category>,
        val excludedExercise: List<Exercise>,
        val excludedEquipment: List<Equipment>
)

data class TrainingTemplateDto(
        val id: String,
        val name: String,
        val exercises: List<TrainingTemplatePartDto>,
        val availableEquipment: List<EquipmentDto>
)

data class TrainingTemplatePartDto(
        val id: String,
        val name: String,
        val category: List<CategoryDto>,
        val excludedExercise: List<ExerciseDto>,
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
        category = category.map { it.toDto() },
        excludedExercise = excludedExercise.map { it.toDto() },
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
        category = category.map { it.toDomain() },
        excludedExercise = excludedExercise.map { it.toDomain() },
        excludedEquipment = excludedEquipment.map { it.toDomain() }
)
data class CreateTrainingTemplate(
        val id: String,
        val name: String,
        val exercises: List<CreateTrainingTemplatePart>,
        val availableEquipmentIds: List<String>
)

data class CreateTrainingTemplatePart(
        val name: String,
        val categoryIds: List<String>,
        val excludedExerciseIds: List<Int>,
        val excludedEquipmentIds: List<String>
)

data class CreateTrainingTemplateDto(
        val id: String,
        val name: String,
        val exercises: List<CreateTrainingTemplatePartDto>,
        val availableEquipmentIds: List<String>
)

data class CreateTrainingTemplatePartDto(
        val name: String,
        val categoryIds: List<String>,
        val excludedExerciseIds: List<Int>,
        val excludedEquipmentIds: List<String>
)

fun CreateTrainingTemplateDto.toDomain() = CreateTrainingTemplate(
        id = id,
        name = name,
        exercises = exercises.map { it.toDomain() },
        availableEquipmentIds = availableEquipmentIds
)

fun CreateTrainingTemplatePartDto.toDomain() = CreateTrainingTemplatePart(
        name = name,
        categoryIds = categoryIds,
        excludedExerciseIds = excludedExerciseIds,
        excludedEquipmentIds = excludedEquipmentIds
)