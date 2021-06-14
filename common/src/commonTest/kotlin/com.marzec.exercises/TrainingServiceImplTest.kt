package com.marzec.exercises

import com.marzec.fiteo.repositories.CategoriesRepository
import com.marzec.fiteo.repositories.EquipmentRepository
import com.marzec.fiteo.repositories.ExercisesRepository
import com.marzec.fiteo.repositories.TrainingRepository
import com.marzec.fiteo.repositories.TrainingTemplateRepository
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest

class TrainingServiceImplTest {

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
        equipmentRepository,
        mockk()
    )

    @BeforeTest
    fun setUp() {
        every { templateRepository.getTemplate(any(), any()) } returns stubTrainingTemplate()
    }
}