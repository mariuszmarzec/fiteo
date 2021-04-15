package com.marzec.exercises

import com.marzec.model.domain.Category
import com.marzec.model.domain.Equipment
import com.marzec.model.domain.Exercise
import com.marzec.model.domain.TrainingTemplate
import com.marzec.model.domain.TrainingTemplatePart
import com.marzec.repositories.CategoriesRepository
import com.marzec.repositories.EquipmentRepository
import com.marzec.repositories.ExercisesRepository
import com.marzec.repositories.TrainingRepository
import com.marzec.repositories.TrainingTemplateRepository
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test

class TrainingServiceImplTest {

    val categoryOne = stubCategory("1", "category_one")
    val categoryTwo = stubCategory("2", "category_two")
    val categoryThree = stubCategory("3", "category_three")

    val equipmentOne = stubEquipment("1", "equipment_one")
    val equipmentTwo = stubEquipment("2", "equipment_two")
    val equipmentThree = stubEquipment("3", "equipment_three")

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

    val trainingTemplate = stubTrainingTemplate(
        id = 0,
        name = "trainingTemplate",
        exercises = listOf(
            stubTrainingTemplatePart(
                id = 1,
                name = "part_one",
                pinnedExercise = null,
                categories = listOf(categoryOne),
                excludedExercises = listOf(2),
                excludedEquipment = listOf()
            ),
            stubTrainingTemplatePart(
                id = 2,
                name = "part_two",
                pinnedExercise = null,
                categories = listOf(categoryTwo),
                excludedExercises = listOf(),
                excludedEquipment = listOf(equipmentTwo)
            )
        ),
        availableEquipment = listOf(equipmentOne, equipmentTwo),
    )

    val templateRepository: TrainingTemplateRepository = mockk()
    val trainingRepository: TrainingRepository = mockk()
    val exercisesRepository: ExercisesRepository = mockk()
    val categoriesRepository: CategoriesRepository = mockk()
    val equipmentRepository: EquipmentRepository = mockk()

    val trainingService = TrainingServiceImpl(
        templateRepository,
        trainingRepository,
        exercisesRepository,
        categoriesRepository,
        equipmentRepository
    )

    @BeforeTest
    fun setUp() {
        every { templateRepository.getTemplate(any(), any()) } returns stubTrainingTemplate()
    }
}

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