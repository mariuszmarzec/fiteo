package com.marzec.model.domain

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