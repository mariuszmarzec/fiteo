package com.marzec.fiteo.model.dto

import com.marzec.fiteo.model.domain.Category
import com.marzec.fiteo.model.domain.CreateExercise
import com.marzec.fiteo.model.domain.Equipment
import com.marzec.fiteo.model.domain.Exercise
import com.marzec.fiteo.model.domain.toDto
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
        val id: String,
        val name: String
)

@Serializable
data class ExerciseDto(
        val id: Int,
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
        val neededEquipment: List<EquipmentDto>,
        val thumbnailName: String?,
        val thumbnailUrl: String?
)

@Serializable
data class CreateExerciseDto(
        val name: String,
        val animationUrl: String?,
        val videoUrl: String?,
        val category: List<CategoryDto>,
        val neededEquipment: List<EquipmentDto>,
        val thumbnailUrl: String?
)

@Serializable
data class EquipmentDto(
        val id: String,
        val name: String
)

fun ExerciseDto.toDomain() = Exercise(
        id = id,
        name = name,
        animationImageName = animationImageName,
        animationUrl = animationUrl,
        videoUrl = videoUrl,
        category = category.map { it.toDomain() },
        imagesNames = imagesNames,
        imagesUrls = imagesUrls,
        descriptionsToImages = descriptionsToImages,
        imagesMistakesUrls = imagesMistakesUrls,
        imagesMistakesNames = imagesMistakesNames,
        descriptionsToMistakes = descriptionsToMistakes,
        muscles = muscles,
        musclesName = musclesName,
        neededEquipment = neededEquipment.map { it.toDomain() },
        thumbnailName = thumbnailName,
        thumbnailUrl = thumbnailUrl
)

fun CategoryDto.toDomain() = Category(
        id = id,
        name = name
)

fun EquipmentDto.toDomain() = Equipment(
        id = id,
        name = name
)

fun CreateExerciseDto.toDomain() = CreateExercise(
      name = name,
      animationUrl = animationUrl,
      videoUrl = videoUrl,
      category = category.map { it.toDomain() },
      neededEquipment = neededEquipment.map { it.toDomain() },
      thumbnailUrl = thumbnailUrl,
)
