package com.marzec.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
        val id: String,
        val name: String
)

@Serializable
data class ExerciseDto(
        val id: String,
        val name: String,
        val animationImageName: String,
        val animationUrl: String,
        val category: CategoryDto,
        val imagesNames: List<String>,
        val imagesUrls: List<String>,
        val neededEquipment: List<EquipmentDto>,
        val thumbnailName: String,
        val thumbnailUrl: String
)

@Serializable
data class EquipmentDto(
        val id: String,
        val name: String
)