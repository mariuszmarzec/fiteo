package com.marzec.model.domain

import com.marzec.model.dto.CategoryDto
import com.marzec.model.dto.EquipmentDto
import com.marzec.model.dto.ExerciseDto

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
        val id: String, // hash
        val name: String,
        val animationImageName: String,
        val animationUrl: String,
        val category: Category,
        val imagesNames: List<String>,
        val imagesUrls: List<String>,
        val neededEquipment: List<Equipment>,
        val thumbnailName: String,
        val thumbnailUrl: String
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
    category = this.category.toDto(),
    imagesNames = this.imagesNames,
    imagesUrls = this.imagesUrls,
    neededEquipment = this.neededEquipment.map { it.toDto() },
    thumbnailName = this.thumbnailName,
    thumbnailUrl = this.thumbnailUrl
)

fun Equipment.toDto() = EquipmentDto(
        id = this.id,
        name = this.name
)