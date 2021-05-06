package com.marzec.exercises

import com.marzec.core.Uuid
import com.marzec.model.domain.Category
import com.marzec.model.domain.Equipment
import com.marzec.model.domain.Exercise
import com.marzec.model.domain.TrainingTemplate
import com.marzec.model.domain.TrainingTemplatePart
import kotlinx.serialization.json.Json

val categoryOne = stubCategory("1", "category_one")
val categoryTwo = stubCategory("2", "category_two")
val categoryThree = stubCategory("3", "category_three")

val equipmentOne = stubEquipment("4", "equipment_one")
val equipmentTwo = stubEquipment("5", "equipment_two")
val equipmentThree = stubEquipment("6", "equipment_three")

val exerciseCategoryOneEquipment0ne = stubExercise(
    id = 1,
    name = "exerciseCategoryOneEquipment0ne",
    category = listOf(categoryOne),
    neededEquipment = listOf(equipmentOne)
)

val exerciseCategoryOneEquipmentTwo = stubExercise(
    id = 2,
    name = "exerciseCategoryOneEquipmentTwo",
    category = listOf(categoryOne),
    neededEquipment = listOf(equipmentTwo)
)

val exerciseCategoryOneEquipmentThree = stubExercise(
    id = 3,
    name = "exerciseCategoryOneEquipmentTwo",
    category = listOf(categoryOne),
    neededEquipment = listOf(equipmentThree)
)

val exerciseCategoryTwoEquipmentOne = stubExercise(
    id = 4,
    name = "exerciseCategoryTwoEquipmentOne",
    category = listOf(categoryTwo),
    neededEquipment = listOf(equipmentOne)
)

val exerciseCategoryTwoEquipmentTwo = stubExercise(
    id = 5,
    name = "exerciseCategoryTwoEquipmentTwo",
    category = listOf(categoryTwo),
    neededEquipment = listOf(equipmentTwo)
)

val exerciseCategoryTwoEquipmentThree = stubExercise(
    id = 6,
    name = "exerciseCategoryTwoEquipmentThree",
    category = listOf(categoryTwo),
    neededEquipment = listOf(equipmentThree)
)

val exerciseCategoryThreeEquipmentThree = stubExercise(
    id = 7,
    name = "exerciseCategoryThreeEquipmentThree",
    category = listOf(categoryThree),
    neededEquipment = listOf(equipmentThree)
)

val categories = listOf(
    categoryOne,
    categoryTwo,
    categoryThree
)

val equipment = listOf(
    equipmentOne,
    equipmentTwo,
    equipmentThree
)

val exercises = listOf(
    exerciseCategoryOneEquipment0ne,
    exerciseCategoryOneEquipmentTwo,
    exerciseCategoryOneEquipmentThree,
    exerciseCategoryTwoEquipmentOne,
    exerciseCategoryTwoEquipmentTwo,
    exerciseCategoryTwoEquipmentThree,
    exerciseCategoryThreeEquipmentThree
)

fun stubTrainingTemplate(
    id: Int = 0,
    name: String = "",
    exercises: List<TrainingTemplatePart> = listOf(),
    availableEquipment: List<Equipment> = listOf()
): TrainingTemplate = TrainingTemplate(
    id = id,
    name = name,
    exercises = exercises,
    availableEquipment = availableEquipment
)

fun stubTrainingTemplatePart(
    id: Int = 0,
    name: String = "",
    pinnedExercise: Exercise? = null,
    categories: List<Category> = emptyList(),
    excludedExercises: List<Int> = emptyList(),
    excludedEquipment: List<Equipment> = emptyList()
) = TrainingTemplatePart(
    id = id,
    name = name,
    pinnedExercise = pinnedExercise,
    categories = categories,
    excludedExercises = excludedExercises,
    excludedEquipment = excludedEquipment
)

fun stubExercise(
    id: Int = 0,
    name: String = "",
    animationImageName: String? = null,
    animationUrl: String? = null,
    videoUrl: String? = null,
    category: List<Category> = listOf(),
    imagesNames: List<String>? = null,
    imagesUrls: List<String>? = null,
    descriptionsToImages: List<String>? = null,
    imagesMistakesUrls: List<String>? = null,
    imagesMistakesNames: List<String>? = null,
    descriptionsToMistakes: List<String>? = null,
    muscles: List<String>? = null,
    musclesName: List<String>? = null,
    neededEquipment: List<Equipment> = listOf(),
    thumbnailName: String? = null,
    thumbnailUrl: String? = null
) = Exercise(
    id = id,
    name = name,
    animationImageName = animationImageName,
    animationUrl = animationUrl,
    videoUrl = videoUrl,
    category = category,
    imagesNames = imagesNames,
    imagesUrls = imagesUrls,
    descriptionsToImages = descriptionsToImages,
    imagesMistakesUrls = imagesMistakesUrls,
    imagesMistakesNames = imagesMistakesNames,
    descriptionsToMistakes = descriptionsToMistakes,
    muscles = muscles,
    musclesName = musclesName,
    neededEquipment = neededEquipment,
    thumbnailName = thumbnailName,
    thumbnailUrl = thumbnailUrl
)

fun stubCategory(
    id: String,
    name: String
) = Category(
    id = id,
    name = name
)

fun stubEquipment(
    id: String,
    name: String
) = Equipment(
    id = id,
    name = name
)

val uuidCounter: Uuid
    get() = object : Uuid {

        private var i = 1

        override fun create(): String {
            return "${i++}"
        }
    }

val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    useArrayPolymorphism = true
}
