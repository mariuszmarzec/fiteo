package com.marzec.model.dto

import kotlinx.serialization.*

@Serializable
data class ExercisesFileDto(
        val category: Map<String, CategoryFileDto>?, // url to category
        val exercises: List<ExerciseFileDto>?,
        val neededEquipment: Map<String, NeededEquipmentDto>?
)

@Serializable
data class CategoryFileDto(
        val category: String,
        val url: String
)

@Serializable
data class ExerciseFileDto(
        val name: String?,
        val animationImageName: String?,
        val animationUrl: String?,
        val category: Map<String, List<String>>?, // key: py/tuple, value: list of url, name
        val imagesNames: List<String>?,
        val imagesUrls: List<String>?,
        val descriptionsToImages: List<String>?,
        val imagesMistakesUrls: List<String>?,
        val imagesMistakesNames: List<String>?,
        val descriptionsToMistakes: List<String>?,
        val muscles: List<String>?,
        val musclesName: List<String>?,
        val neededEquipment: NeededEquipmentDto?,
        val thumbnailName: String?,
        val thumbnailUrl: String?,
        val url: String?
)

@Serializable
data class NeededEquipmentDto(
        val needed: List<String>?,
        val pageUrl: String?,
        val thumbnail: String?,
        val url: String?
)