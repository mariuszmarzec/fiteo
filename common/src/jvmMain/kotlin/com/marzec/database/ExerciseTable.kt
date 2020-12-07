package com.marzec.database

import com.marzec.database.ExerciseEntity.Companion.transform
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
    val animationImageName = varchar("animationImageName", length = 1000).nullable()
    val animationUrl = varchar("animationUrl", length = 1000).nullable()
    val videoUrl = varchar("videoUrl", length = 1000).nullable()
    val imagesNames = text("imagesNames")
    val imagesUrls = text("imagesUrls")
    val descriptionsToImages = text("descriptionsToImages")
    val imagesMistakesUrls = text("imagesMistakesUrls")
    val imagesMistakesNames = text("imagesMistakesNames")
    val descriptionsToMistakes = text("descriptionsToMistakes")
    val muscles = text("muscles")
    val musclesName = text("musclesName")
    val thumbnailName = varchar("thumbnailName", length = 1000).nullable()
    val thumbnailUrl = varchar("thumbnailUrl", length = 1000).nullable()
}

class ExerciseEntity(id: EntityID<Int>) : IntEntity(id) {

    val name by ExerciseTable.name
    val animationImageName: List<String>? by ExerciseTable.animationImageName.transformStringListNullable()
    val animationUrl by ExerciseTable.animationUrl
    val videoUrl by ExerciseTable.videoUrl
    val category by CategoryEntity via CategoriesToExercisesTable
    val imagesNames by ExerciseTable.imagesNames.transformStringList()
    val imagesUrls by ExerciseTable.imagesUrls.transformStringList()
    val descriptionsToImages by ExerciseTable.descriptionsToImages.transformStringList()
    val imagesMistakesUrls by ExerciseTable.imagesMistakesUrls.transformStringList()
    val imagesMistakesNames by ExerciseTable.imagesMistakesNames.transformStringList()
    val descriptionsToMistakes by ExerciseTable.descriptionsToMistakes.transformStringList()
    val muscles by ExerciseTable.muscles.transformStringList()
    val musclesName by ExerciseTable.musclesName.transformStringList()
    val neededEquipment by ExerciseEntity via ExercisesToEquipment
    val thumbnailName by ExerciseTable.thumbnailName
    val thumbnailUrl by ExerciseTable.thumbnailUrl

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
        { list -> Json.encodeToString(list) },
        { value -> value?.let { Json.decodeFromString<List<String>>(it) } }
)

fun Column<String>.transformStringList() = transform(
        { list -> Json.encodeToString(list) },
        { value -> Json.decodeFromString<List<String>>(value) }
)

