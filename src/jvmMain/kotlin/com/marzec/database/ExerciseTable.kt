package com.marzec.database

import com.marzec.database.ExerciseEntity.Companion.transform
import com.marzec.fiteo.model.domain.Exercise
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object ExerciseTable : IntIdTable("exercises") {
    val name = varchar("name", length = 300)
    val animationImageName = varchar("animation_image_name", length = 1000).nullable()
    val animationUrl = varchar("animation_url", length = 1000).nullable()
    val videoUrl = varchar("video_url", length = 1000).nullable()
    val imagesNames = text("images_names").nullable()
    val imagesUrls = text("images_urls").nullable()
    val descriptionsToImages = text("descriptions_to_images").nullable()
    val imagesMistakesUrls = text("images_Mistakes_urls").nullable()
    val imagesMistakesNames = text("images_Mistakes_names").nullable()
    val descriptionsToMistakes = text("descriptions_to_mistakes").nullable()
    val muscles = text("muscles").nullable()
    val musclesName = text("muscles_name").nullable()
    val thumbnailName = varchar("thumbnail_name", length = 1000).nullable()
    val thumbnailUrl = varchar("thumbnail_url", length = 1000).nullable()
}

class ExerciseEntity(id: EntityID<Int>) : IntEntity(id) {

    var name by ExerciseTable.name
    var animationImageName by ExerciseTable.animationImageName
    var animationUrl by ExerciseTable.animationUrl
    var videoUrl by ExerciseTable.videoUrl
    var category by CategoryEntity via CategoriesToExercisesTable
    var imagesNames by ExerciseTable.imagesNames.transformStringListNullable()
    var imagesUrls by ExerciseTable.imagesUrls.transformStringListNullable()
    var descriptionsToImages by ExerciseTable.descriptionsToImages.transformStringListNullable()
    var imagesMistakesUrls by ExerciseTable.imagesMistakesUrls.transformStringListNullable()
    var imagesMistakesNames by ExerciseTable.imagesMistakesNames.transformStringListNullable()
    var descriptionsToMistakes by ExerciseTable.descriptionsToMistakes.transformStringListNullable()
    var muscles by ExerciseTable.muscles.transformStringListNullable()
    var musclesName by ExerciseTable.musclesName.transformStringListNullable()
    var neededEquipment by EquipmentEntity via ExercisesToEquipment
    var thumbnailName by ExerciseTable.thumbnailName
    var thumbnailUrl by ExerciseTable.thumbnailUrl

    fun toDomain() = Exercise(
            id = id.value,
            name = name,
            animationImageName = animationImageName,
            animationUrl = animationUrl,
            videoUrl = videoUrl,
            category = category.map { it.toDomain() }.toList(),
            imagesNames = imagesNames,
            imagesUrls = imagesUrls,
            descriptionsToImages = descriptionsToImages,
            imagesMistakesUrls = imagesMistakesUrls,
            imagesMistakesNames = imagesMistakesNames,
            descriptionsToMistakes = descriptionsToMistakes,
            muscles = muscles,
            musclesName = musclesName,
            neededEquipment = neededEquipment.map { it.toDomain() }.toList(),
            thumbnailName = thumbnailName,
            thumbnailUrl = thumbnailUrl
    )

    companion object : IntEntityClass<ExerciseEntity>(ExerciseTable)
}

object CategoriesToExercisesTable : IntIdTable("categories_to_exercises") {
    val category = reference("category_id", CategoryTable)
    val exercise = reference("exercise_id", ExerciseTable)
}

object ExercisesToEquipment : IntIdTable("exercises_to_equipment") {
    val exercise = reference("exercise_id", ExerciseTable)
    val equipment = reference("equipment_id", EquipmentTable)
}

fun Column<String?>.transformStringListNullable() = transform(
    toColumn = { list -> list?.let { Json.encodeToString(list) } },
    toReal = { value -> value?.let { Json.decodeFromString<List<String>>(it) } }
)
