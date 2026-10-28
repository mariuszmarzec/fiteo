package com.marzec.fiteo.model.domain

import com.marzec.fiteo.model.dto.CategoryDto
import com.marzec.fiteo.model.dto.EquipmentDto
import com.marzec.fiteo.model.dto.ExerciseDto

data class ExercisesData(
        val categories: List<Category>,
        val exercises: List<Exercise>,
        val equipment: List<Equipment>
)

data class Category(
        val id: String, // hash
        val name: String
)

data class Exercise(
        val id: Int,
        val name: String,
        val animationImageName: String?,
        val animationUrl: String?,
        val videoUrl: String?,
        val category: List<Category>,
        val imagesNames: List<String>?,
        val imagesUrls: List<String>?,
        val descriptionsToImages: List<String>?,
        val imagesMistakesUrls: List<String>?,
        val imagesMistakesNames: List<String>?,
        val descriptionsToMistakes: List<String>?,
        val muscles: List<String>?,
        val musclesName: List<String>?,
        val neededEquipment: List<Equipment>,
        val thumbnailName: String?,
        val thumbnailUrl: String?
)

data class Equipment(
        val id: String, // hash z nazwy
        val name: String
)

fun Category.toDto() = CategoryDto(
        id,
        name
)

fun Exercise.toDto() = ExerciseDto(
        id = this.id,
        name = this.name,
        animationImageName = this.animationImageName,
        animationUrl = this.animationUrl,
        videoUrl = this.videoUrl,
        category = this.category.map { it.toDto() },
        imagesNames = this.imagesNames,
        imagesUrls = this.imagesUrls,
        descriptionsToImages = this.descriptionsToImages,
        imagesMistakesUrls = this.imagesMistakesUrls,
        imagesMistakesNames = this.imagesMistakesNames,
        descriptionsToMistakes = this.descriptionsToMistakes,
        muscles = this.muscles,
        musclesName = this.musclesName,
        neededEquipment = this.neededEquipment.map { it.toDto() },
        thumbnailName = this.thumbnailName,
        thumbnailUrl = this.thumbnailUrl
)

fun Equipment.toDto() = EquipmentDto(
        id = this.id,
        name = this.name
)
