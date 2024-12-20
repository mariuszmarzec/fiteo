package com.marzec

import com.marzec.cheatday.dto.PutWeightDto
import com.marzec.cheatday.dto.WeightDto
import com.marzec.core.Uuid
import com.marzec.core.currentTime
import com.marzec.core.model.domain.FeatureToggle
import com.marzec.core.model.dto.NewFeatureToggleDto
import com.marzec.fiteo.model.domain.Category
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
import com.marzec.fiteo.model.domain.UpdateTrainingDto
import com.marzec.fiteo.model.domain.UpdateTrainingExerciseWithProgressDto
import com.marzec.fiteo.model.domain.toDto
import com.marzec.fiteo.model.dto.CategoryDto
import com.marzec.fiteo.model.dto.EquipmentDto
import com.marzec.fiteo.model.dto.ExerciseDto
import com.marzec.fiteo.model.dto.LoginRequestDto
import com.marzec.fiteo.model.dto.RegisterRequestDto
import com.marzec.todo.dto.TaskDto
import com.marzec.todo.model.CreateTask
import com.marzec.todo.model.CreateTaskDto
import com.marzec.todo.model.Scheduler
import com.marzec.todo.model.SchedulerDto
import com.marzec.todo.model.Task
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.isoDayNumber
import kotlinx.serialization.json.Json

val creationTime = "2021-05-15T00:00:00"
val dateTime = "2021-05-16T00:00:00"
val dateTime2 = "2021-05-17T07:20:30"
val dateTime3 = "2021-05-18T07:20:30"
val dateTime4 = "2021-05-19T00:00:00"

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
    categoryThree,
    categoryTwo
)

val equipment = listOf(
    equipmentOne,
    equipmentThree,
    equipmentTwo
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

val createWeightDto = PutWeightDto(60f, dateTime)

val weightDto = WeightDto(1, 60f, dateTime)

val createWeightDto2 = PutWeightDto(61f, dateTime2)

val weightDto2 = WeightDto(2, 61f, dateTime2)

val createWeightDto3 = PutWeightDto(60.5f, dateTime3)

val weightDto3 = WeightDto(3, 60.5f, dateTime3)

val createTaskDto = stubCreateTaskDto("task", null, 0)

fun stubCreateTaskDto(
    description: String = "",
    parentTaskId: Int? = null,
    priority: Int = 0,
    scheduler: SchedulerDto? = null
) = CreateTaskDto(
    description = description,
    parentTaskId = parentTaskId,
    priority = priority,
    scheduler = scheduler
)

fun stubCreateTask(
    description: String = "",
    parentTaskId: Int? = null,
    priority: Int? = null,
    scheduler: Scheduler? = null,
    highestPriorityAsDefault: Boolean = false
) = CreateTask(
    description = description,
    parentTaskId = parentTaskId,
    priority = priority,
    scheduler = scheduler,
    highestPriorityAsDefault = highestPriorityAsDefault
)

val taskDto = stubTaskDto(
    id = 1,
    description = "task"
)

fun stubTaskDto(
    id: Int = 1,
    description: String = "",
    addedTime: String = dateTime,
    modifiedTime: String = dateTime,
    parentTaskId: Int? = null,
    subTasks: List<TaskDto> = emptyList(),
    isToDo: Boolean = true,
    priority: Int = 0,
    scheduler: SchedulerDto? = null
) = TaskDto(
    id = id,
    description = description,
    addedTime = addedTime,
    modifiedTime = modifiedTime,
    parentTaskId = parentTaskId,
    subTasks = subTasks,
    isToDo = isToDo,
    priority = priority,
    scheduler = scheduler
)

fun stubTask(
    id: Int = 1,
    description: String = "",
    addedTime: LocalDateTime = currentTime(),
    modifiedTime: LocalDateTime = currentTime(),
    parentTaskId: Int? = null,
    subTasks: List<Task> = emptyList(),
    isToDo: Boolean = true,
    priority: Int = 0,
    scheduler: Scheduler? = null
) = Task(
    id = id,
    description = description,
    addedTime = addedTime,
    modifiedTime = modifiedTime,
    parentTaskId = parentTaskId,
    subTasks = subTasks,
    isToDo = isToDo,
    priority = priority,
    scheduler = scheduler
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
    id: Int? = null,
    name: String,
    pinnedExerciseId: Int? = null,
    categoryIds: List<String> = emptyList(),
    excludedExercisesIds: List<Int> = emptyList(),
    excludedEquipmentIds: List<String> = emptyList()
) = CreateTrainingTemplatePartDto(
    id = id,
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
    createDateInMillis: String = dateTime,
    finishDateInMillis: String = dateTime,
    exercisesWithProgress: List<TrainingExerciseWithProgressDto> = emptyList()
) = TrainingDto(
    id = id,
    templateId = templateId,
    createDateInMillis = createDateInMillis,
    finishDateInMillis = finishDateInMillis,
    exercisesWithProgress = exercisesWithProgress
)

fun stubTrainingExerciseWithProgressDto(
    id: Int = 0,
    exercise: ExerciseDto = stubExerciseDto(),
    series: List<SeriesDto> = emptyList(),
    templatePartId: Int? = null,
    name: String
) = TrainingExerciseWithProgressDto(
    id = id,
    exercise = exercise,
    series = series,
    templatePartId = templatePartId,
    name = name
)

fun stubSeriesDto(
    seriesId: Int = 0,
    exerciseId: Int = 0,
    trainingId: Int = 0,
    date: String = dateTime,
    burden: Float? = null,
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

fun stubUpdateTrainingDto(
    finishDateInMillis: String = dateTime,
    exercisesWithProgress: List<UpdateTrainingExerciseWithProgressDto> = emptyList()
) = UpdateTrainingDto(
    finishDateInMillis,
    exercisesWithProgress
)

fun stubUpdateTrainingExerciseWithProgressDto(
    id: Int? = null,
    exerciseId: Int = 0,
    series: List<SeriesDto> = emptyList(),
    trainingPartId: Int? = null,
    name: String
) = UpdateTrainingExerciseWithProgressDto(
    id = id,
    exerciseId = exerciseId,
    series = series,
    trainingPartId = trainingPartId,
    name = name
)

val schedulerOneShotDto = SchedulerDto(
    hour = 7,
    minute = 0,
    creationDate = creationTime,
    startDate = dateTime,
    lastDate = dateTime,
    daysOfWeek = emptyList(),
    dayOfMonth = 0,
    repeatCount = -1,
    repeatInEveryPeriod = 1,
    type = Scheduler.OneShot::class.simpleName.toString(),
)

val schedulerWeeklyDto = SchedulerDto(
    hour = 12,
    minute = 0,
    creationDate = creationTime,
    startDate = dateTime,
    lastDate = dateTime,
    daysOfWeek = listOf(DayOfWeek.MONDAY.isoDayNumber, DayOfWeek.FRIDAY.isoDayNumber),
    dayOfMonth = 0,
    repeatCount = 3,
    repeatInEveryPeriod = 3,
    type = Scheduler.Weekly::class.simpleName.toString(),
)

val schedulerMonthlyDto = SchedulerDto(
    hour = 14,
    minute = 30,
    creationDate = creationTime,
    startDate = dateTime,
    lastDate = dateTime,
    daysOfWeek = emptyList(),
    dayOfMonth = 27,
    repeatCount = 5,
    repeatInEveryPeriod = 1,
    type = Scheduler.Monthly::class.simpleName.toString(),
)

val featureToggleOne = FeatureToggle(1, "featureToggleOne", "100")
val featureToggleTwo = FeatureToggle(2, "featureToggleTwo", "true")
val featureToggleThree = FeatureToggle(3, "featureToggleThree", "false")

val featureToggles = listOf(featureToggleOne, featureToggleTwo, featureToggleThree)

val newFeatureToggle = NewFeatureToggleDto("featureToggleOne", "100")

fun FeatureToggle.toCreate() = NewFeatureToggleDto(name, value)