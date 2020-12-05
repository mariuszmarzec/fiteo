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
        val animationImageName: String?,
        val animationUrl: String?,
        val videoUrl: String?,
        val category: List<CategoryDto>,
        val imagesNames: List<String>?,
        val imagesUrls: List<String>?,
        val descriptionsToImages: List<String>?,
        val imagesMistakesUrls: List<String>?,
        val imagesMistakesNames: List<String>?,
        val descriptionsToMistakes: List<String>?,
        val muscles: List<String>?,
        val musclesName: List<String>?,
        val neededEquipment: List<EquipmentDto>?,
        val thumbnailName: String?,
        val thumbnailUrl: String?
)

@Serializable
data class EquipmentDto(
        val id: String,
        val name: String
)