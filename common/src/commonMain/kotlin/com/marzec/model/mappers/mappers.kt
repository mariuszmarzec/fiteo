package com.marzec.model.mappers

import com.marzec.model.domain.Category
import com.marzec.model.domain.Equipment
import com.marzec.model.domain.Exercise
import com.marzec.model.domain.ExercisesData
import com.marzec.model.domain.toDto
import com.marzec.model.dto.CategoryFileDto
import com.marzec.model.dto.ExerciseFileDto
import com.marzec.model.dto.ExercisesFileDto
import com.marzec.model.dto.NeededEquipmentDto

fun ExercisesFileDto.toDomain() = ExercisesData(
        category?.map { it.value.toDomain() }.orEmpty(),
        exercises?.map { it.toDomain() }.orEmpty(),
        listOf(noEquipment) + neededEquipment?.map { it.value.toDomain() }?.flatten()?.distinct().orEmpty()
)

fun CategoryFileDto.toDomain() = Category(category.hashCode().toString(), category)

fun NeededEquipmentDto.toDomain() = needed?.map { equipment ->
    Equipment(equipment.hashCode().toString(), equipment)
}.orEmpty()

fun ExerciseFileDto.toDomain() = Exercise(
        id = url.hashCode().toString(),
        name = name.orEmpty(),
        animationImageName = animationImageName.orEmpty(),
        animationUrl = animationUrl.orEmpty(),
        category = category?.values?.firstOrNull()?.getOrNull(1)?.let {
            categoryName -> Category(categoryName.hashCode().toString(), categoryName)
        } ?: unknownCategory,
        imagesNames = imagesNames.orEmpty(),
        imagesUrls = imagesUrls.orEmpty(),
        descriptionsToImages = this.descriptionsToImages.orEmpty(),
        imagesMistakesUrls = this.imagesMistakesUrls.orEmpty(),
        imagesMistakesNames = this.imagesMistakesNames.orEmpty(),
        descriptionsToMistakes = this.descriptionsToMistakes.orEmpty(),
        muscles = this.muscles.orEmpty(),
        musclesName = this.musclesName.orEmpty(),
        neededEquipment = neededEquipment?.toDomain() ?: listOf(noEquipment),
        thumbnailName = thumbnailName.orEmpty(),
        thumbnailUrl = thumbnailUrl.orEmpty()
)

private val unknownCategory = Category("Unknown".hashCode().toString(), "Unknown")

private val noEquipment = Equipment("Brak".hashCode().toString(), "Brak")