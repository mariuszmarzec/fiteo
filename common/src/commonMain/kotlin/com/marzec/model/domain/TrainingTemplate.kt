package com.marzec.model.domain

import com.marzec.model.dto.CategoryDto
import com.marzec.model.dto.EquipmentDto
import com.marzec.model.dto.ExerciseDto

data class TrainingTemplate(
        val id: String,
        val userId: String,
        val name: String,
        val exercises: List<TrainingTemplatePart>,
        val availableEquipment: List<Equipment>
)

data class TrainingTemplatePart(
        val category: Category,
        val excludedExercise: List<Exercise>,
        val excludedEquipment: List<Equipment>
)

data class TrainingTemplateDto(
        val id: String,
        val userId: String,
        val name: String,
        val exercises: List<TrainingTemplatePartDto>,
        val availableEquipment: List<EquipmentDto>
)

data class TrainingTemplatePartDto(
        val category: CategoryDto,
        val excludedExercise: List<ExerciseDto>,
        val excludedEquipment: List<EquipmentDto>
)

fun TrainingTemplate.toDto() = TrainingTemplateDto(
        id = id,
        userId = userId,
        name = name,
        exercises = exercises.map { it.toDto() },
        availableEquipment = availableEquipment.map { it.toDto() }
)

fun TrainingTemplatePart.toDto() = TrainingTemplatePartDto(
        category = category.toDto(),
        excludedExercise = excludedExercise.map { it.toDto() },
        excludedEquipment = excludedEquipment.map { it.toDto() }
)