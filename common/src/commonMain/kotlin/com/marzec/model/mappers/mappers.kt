package com.marzec.model.mappers

import com.marzec.core.Uuid
import com.marzec.model.domain.Category
import com.marzec.model.domain.Equipment
import com.marzec.model.domain.Exercise
import com.marzec.model.domain.ExercisesData
import com.marzec.model.dto.CategoryFileDto
import com.marzec.model.dto.ExerciseFileDto
import com.marzec.model.dto.ExercisesFileDto
import com.marzec.model.dto.NeededEquipmentDto

fun ExercisesFileDto.toDomain(uuid: Uuid): ExercisesData {
    val unknownCategory = Category(uuid.create(), "Unknown")
    val hashToCategories = category?.map {
        val categoryFileDto = it.value
        categoryFileDto.category.hashCode().toString() to categoryFileDto.toDomain(uuid)
    }.orEmpty().toMap() + mapOf("Unknown".hashCode().toString() to unknownCategory)
    val equipment = neededEquipment?.map { it.value.toDomain(uuid) }?.flatten()?.distinct().orEmpty()
    val hashToEquipment = equipment.map { it.name.hashCode().toString() to it }.toMap()
    return ExercisesData(
            hashToCategories.values.toList(),
            exercises?.map { it.toDomain(hashToCategories, hashToEquipment, unknownCategory) }.orEmpty(),
            equipment
    )
}

fun CategoryFileDto.toDomain(uuid: Uuid) = Category(uuid.create(), category)

fun NeededEquipmentDto.toDomain(uuid: Uuid): List<Equipment> = needed?.map { equipment ->
    Equipment(uuid.create(), equipment)
}.orEmpty()

fun ExerciseFileDto.toDomain(
        hashToCategories: Map<String, Category>,
        hashToEquipment: Map<String, Equipment>,
        unknownCategory: Category
): Exercise {
    val defaultEquipment = hashToEquipment.values.firstOrNull { it.name.contains("brak", true) }?.let { listOf(it) }
    return Exercise(
            id = url.hashCode().toString(),
            name = name.orEmpty(),
            animationImageName = animationImageName.orEmpty(),
            animationUrl = animationUrl.orEmpty(),
            category = listOf(category?.values?.firstOrNull()?.getOrNull(1)?.let { categoryName ->
                hashToCategories[categoryName.hashCode().toString()]
            } ?: unknownCategory),
            imagesNames = imagesNames.orEmpty(),
            imagesUrls = imagesUrls.orEmpty(),
            descriptionsToImages = this.descriptionsToImages.orEmpty(),
            imagesMistakesUrls = this.imagesMistakesUrls.orEmpty(),
            imagesMistakesNames = this.imagesMistakesNames.orEmpty(),
            descriptionsToMistakes = this.descriptionsToMistakes.orEmpty(),
            muscles = this.muscles.orEmpty(),
            musclesName = this.musclesName.orEmpty(),
            neededEquipment = neededEquipment?.needed?.mapNotNull { hashToEquipment[it.hashCode().toString()] } ?: defaultEquipment
            ?: emptyList(),
            thumbnailName = thumbnailName.orEmpty(),
            thumbnailUrl = thumbnailUrl.orEmpty()
    )
}