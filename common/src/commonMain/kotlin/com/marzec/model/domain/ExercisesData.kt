package com.marzec.model.domain

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