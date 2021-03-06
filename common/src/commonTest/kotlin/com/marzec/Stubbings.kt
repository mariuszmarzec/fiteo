package com.marzec

import com.marzec.cheatday.dto.PutWeightDto
import com.marzec.cheatday.dto.WeightDto
import com.marzec.core.Uuid
import com.marzec.fiteo.model.domain.Category
import com.marzec.fiteo.model.domain.CreateTrainingDto
import com.marzec.fiteo.model.domain.CreateTrainingExerciseWithProgressDto
import com.marzec.fiteo.model.domain.CreateTrainingTemplateDto
import com.marzec.fiteo.model.domain.CreateTrainingTemplatePartDto
import com.marzec.fiteo.model.domain.Equipment
import com.marzec.fiteo.model.domain.Exercise
import com.marzec.fiteo.model.domain.SeriesDto
import com.marzec.fiteo.model.domain.TrainingDto
import com.marzec.fiteo.model.domain.TrainingExerciseWithProgressDto
import com.marzec.fiteo.model.domain.TrainingTemplate
import com.marzec.fiteo.model.domain.TrainingTemplateDto
import com.marzec.fiteo.model.domain.TrainingTemplatePart
import com.marzec.fiteo.model.domain.TrainingTemplatePartDto
import com.marzec.fiteo.model.domain.toDto
import com.marzec.fiteo.model.dto.CategoryDto
import com.marzec.fiteo.model.dto.EquipmentDto
import com.marzec.fiteo.model.dto.ExerciseDto
import com.marzec.fiteo.model.dto.LoginRequestDto
import com.marzec.fiteo.model.dto.RegisterRequestDto
import com.marzec.todo.dto.CreateTodoListDto
import com.marzec.todo.dto.TaskDto
import com.marzec.todo.dto.ToDoListDto
import com.marzec.todo.model.CreateTaskDto
import com.marzec.todo.model.UpdateTaskDto
import kotlinx.serialization.json.Json

val categoryOne = stubCategory("1", "category_one")
val categoryTwo = stubCategory("2", "category_two")
val categoryThree = stubCategory("3", "category_three")

val equipmentOne = stubEquipment("4", "equipment_one")
val equipmentTwo = stubEquipment("5", "equipment_two")
val equipmentThree = stubEquipment("6", "equipment_three")

val categoryOneDto = categoryOne.toDto()
val categoryTwoDto = categoryTwo.toDto()
val categoryThreeDto = categoryThree.toDto()

val equipmentOneDto = equipmentOne.toDto()
val equipmentTwoDto = equipmentTwo.toDto()
val equipmentThreeDto = equipmentThree.toDto()

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

fun stubRegisterRequestDto(
    email: String = "",
    password: String = "",
    repeatedPassword: String = ""
) = RegisterRequestDto(
    email = email,
    password = password,
    repeatedPassword = repeatedPassword
)

val registerRequestDto = stubRegisterRequestDto(
    email = "test@mail.com",
    password = "1234567890",
    repeatedPassword = "1234567890"
)

val loginDto = LoginRequestDto(email = "test@mail.com", password = "1234567890")

val createWeightDto = PutWeightDto(60f, "2021-05-15T07:20:30")

val weightDto = WeightDto(1, 60f, "2021-05-15T07:20:30")

val createWeightDto2 = PutWeightDto(61f, "2021-05-16T07:20:30")

val weightDto2 = WeightDto(2, 61f, "2021-05-16T07:20:30")

val createWeightDto3 = PutWeightDto(60.5f, "2021-05-17T07:20:30")

val weightDto3 = WeightDto(3, 60.5f, "2021-05-17T07:20:30")

val createTodoListDto = CreateTodoListDto("todo list 1")

val createTodoListDto2 = CreateTodoListDto("todo list 2")

val todoListDto = ToDoListDto(1, "todo list 1", emptyList())

val todoListDto2 = ToDoListDto(2, "todo list 2", emptyList())

val createTaskDto = stubCreateTaskDto("task", null, 0)

fun stubCreateTaskDto(
    description: String = "",
    parentTaskId: Int? = null,
    priority: Int = 0
) = CreateTaskDto(
    description = description,
    parentTaskId = parentTaskId,
    priority = priority,
)

val taskDto = stubTaskDto(
    id = 1,
    description = "task"
)

fun stubTaskDto(
    id: Int = 1,
    description: String = "",
    addedTime: String = "2021-05-16T00:00:00",
    modifiedTime: String = "2021-05-16T00:00:00",
    parentTaskId: Int? = null,
    subTasks: List<TaskDto> = emptyList(),
    isToDo: Boolean = true,
    priority: Int = 0
) = TaskDto(
    id = id,
    description = description,
    addedTime = addedTime,
    modifiedTime = modifiedTime,
    parentTaskId = parentTaskId,
    subTasks = subTasks,
    isToDo = isToDo,
    priority = priority
)

fun stubUpdateTaskDto(
    description: String = "",
    parentTaskId: Int? = null,
    priority: Int = 0,
    isToDo: Boolean = true
) = UpdateTaskDto(
    description = description,
    parentTaskId = parentTaskId,
    priority = priority,
    isToDo = isToDo
)

fun stubCreateTrainingTemplateDto(
    id: Int = 0,
    name: String = "",
    exercises: List<CreateTrainingTemplatePartDto> = emptyList(),
    availableEquipmentIds: List<String> = emptyList()
) = CreateTrainingTemplateDto(
    id,
    name,
    exercises,
    availableEquipmentIds,
)

fun stubCreateTrainingTemplatePartDto(
    name: String,
    pinnedExerciseId: Int? = null,
    categoryIds: List<String> = emptyList(),
    excludedExercisesIds: List<Int> = emptyList(),
    excludedEquipmentIds: List<String> = emptyList()
) = CreateTrainingTemplatePartDto(
    name = name,
    pinnedExerciseId = pinnedExerciseId,
    categoryIds = categoryIds,
    excludedExercisesIds = excludedExercisesIds,
    excludedEquipmentIds = excludedEquipmentIds,
)

fun stubTrainingTemplateDto(
    id: Int = 0,
    name: String = "",
    exercises: List<TrainingTemplatePartDto> = emptyList(),
    availableEquipment: List<EquipmentDto> = emptyList()
) = TrainingTemplateDto(
    id = id,
    name = name,
    exercises = exercises,
    availableEquipment = availableEquipment,
)

fun stubTrainingTemplatePartDto(
    id: Int = 0,
    name: String = "",
    pinnedExercise: ExerciseDto? = null,
    categories: List<CategoryDto> = emptyList(),
    excludedExercises: List<Int> = emptyList(),
    excludedEquipment: List<EquipmentDto> = emptyList()
) = TrainingTemplatePartDto(
    id = id,
    name = name,
    pinnedExercise = pinnedExercise,
    categories = categories,
    excludedExercises = excludedExercises,
    excludedEquipment = excludedEquipment
)

fun stubTraining(
    id: Int = 0,
    templateId: Int = 0,
    createDateInMillis: String = "2021-05-16T00:00:00",
    finishDateInMillis: String = "2021-05-16T00:00:00",
    exercisesWithProgress: List<TrainingExerciseWithProgressDto> = emptyList()
) = TrainingDto(
    id = id,
    templateId = templateId,
    createDateInMillis = createDateInMillis,
    finishDateInMillis = finishDateInMillis,
    exercisesWithProgress = exercisesWithProgress
)

fun stubTrainingExerciseWithProgressDto(
    exercise: ExerciseDto = stubExerciseDto(),
    series: List<SeriesDto> = emptyList()
) = TrainingExerciseWithProgressDto(
    exercise = exercise,
    series = series,
)

fun stubSeriesDto(
    seriesId: Int = 0,
    exerciseId: Int = 0,
    trainingId: Int = 0,
    date: String = "2021-05-16T00:00:00",
    burden: Int? = null,
    timeInMillis: Long? = null,
    repsNumber: Int? = null,
    note: String = ""
) = SeriesDto(
    seriesId = seriesId,
    exerciseId = exerciseId,
    trainingId = trainingId,
    date = date,
    burden = burden,
    timeInMillis = timeInMillis,
    repsNumber = repsNumber,
    note = note,
)

fun stubExerciseDto(
    id: Int = 0,
    name: String = "",
    animationImageName: String? = null,
    animationUrl: String? = null,
    videoUrl: String? = null,
    category: List<CategoryDto> = emptyList(),
    imagesNames: List<String>? = null,
    imagesUrls: List<String>? = null,
    descriptionsToImages: List<String>? = null,
    imagesMistakesUrls: List<String>? = null,
    imagesMistakesNames: List<String>? = null,
    descriptionsToMistakes: List<String>? = null,
    muscles: List<String>? = null,
    musclesName: List<String>? = null,
    neededEquipment: List<EquipmentDto> = emptyList(),
    thumbnailName: String? = null,
    thumbnailUrl: String? = null
) = ExerciseDto(
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

fun stubCreateTrainingDto(
    finishDateInMillis: String = "2021-05-16T00:00:00",
    exercisesWithProgress: List<CreateTrainingExerciseWithProgressDto> = emptyList()
) = CreateTrainingDto(
    finishDateInMillis,
    exercisesWithProgress
)

fun stubCreateTrainingExerciseWithProgressDto(
    exerciseId: Int = 0,
    series: List<SeriesDto> = emptyList()
) = CreateTrainingExerciseWithProgressDto(
   exerciseId, series
)