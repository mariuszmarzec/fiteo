package com.marzec

import com.marzec.extensions.emptyString
import com.marzec.extensions.replace
import com.marzec.extensions.replaceIf
import com.marzec.model.domain.Exercise
import com.marzec.model.dto.CategoryDto
import com.marzec.model.dto.ExerciseDto
import com.marzec.model.domain.Category
import com.marzec.model.domain.Equipment
import com.marzec.model.dto.EquipmentDto
import com.marzec.model.dto.toDomain
import io.ktor.client.request.get
import kotlin.reflect.KClass
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import react.RProps
import react.child
import react.dom.div
import react.dom.h1
import react.dom.h3
import react.dom.img
import react.dom.input
import react.dom.label
import react.dom.render
import react.functionalComponent
import react.useEffect
import react.useState


val endpoint = window.location.origin

suspend fun getExercises(): List<Exercise> =
    jsonClient.get<List<ExerciseDto>>(endpoint + ApiPath.EXERCISES)
        .map { it.toDomain() }

suspend fun getCategories(): List<Category> =
    jsonClient.get<List<CategoryDto>>(endpoint + ApiPath.CATEGORIES)
        .map { it.toDomain() }

suspend fun getEquipment(): List<Equipment> =
    jsonClient.get<List<EquipmentDto>>(endpoint + ApiPath.EQUIPMENT)
        .map { it.toDomain() }

@ExperimentalCoroutinesApi
fun main() {
    render(document.getElementById("root")) {
        child(App)
    }
}

private val scope = MainScope()

val defaultState = State.Loading<ExercisesListViewState>()

@ExperimentalCoroutinesApi
val exerciseListStore = Store<ExercisesListViewState, ExercisesListActions>(defaultState).apply {
    intents = mapOf(
        ExercisesListActions.Initialization::class to intent(
            onTrigger = {
                ExercisesData(
                    getExercises(),
                    getCategories(),
                    getEquipment()
                )
            },
            reducer = { _: ExercisesListActions, actionResult: ExercisesData?, _: State<ExercisesListViewState> ->
                actionResult?.let { exercisesData ->
                    State.Data(
                        ExercisesListViewState(
                            exercises = exercisesData.exercises,
                            categories = exercisesData.categories.map {
                                CategoryCheckboxViewModel(
                                    category = it,
                                    isChecked = false
                                )
                            },
                            equipment = exercisesData.equipment,
                            groupedExercises = exercisesData.exercises.groupByCategories()
                        )
                    )
                } ?: State.Error("Data loading error")
            },
            sideEffect = { _: ExercisesData?, _: State<ExercisesListViewState> ->
                console.log("Data loaded!")
            }
        ),
        ExercisesListActions.OnCategoryCheckedChange::class to intent(
            reducer = { action: ExercisesListActions.OnCategoryCheckedChange, _: Any?, currentState: State<ExercisesListViewState> ->
                when (currentState) {
                    is State.Data -> {
                        val categories = currentState.data.categories
                            .replaceIf({ it.category.id == action.categoryId }) { category ->
                                category.copy(isChecked = !category.isChecked)
                            }
                        val exercises = currentState.data.exercises
                            .filterByCategories(categories)
                            .groupByCategories()
                        State.Data(
                            currentState.data.copy(
                                categories = categories,
                                groupedExercises = exercises
                            )
                        )
                    }
                    is State.Loading -> currentState.copy()
                    is State.Error -> currentState.copy()
                }
            }
        )
    )
}

private fun List<Exercise>.filterByCategories(categories: List<CategoryCheckboxViewModel>): List<Exercise> {
    return if (categories.all { !it.isChecked }) {
        this
    } else {
        val targetCategories = categories.filter { it.isChecked }.map { it.category.id }
        filter { exercise ->
            val exercisesCategory = exercise.category.map { it.id }
            exercisesCategory.containsAll(targetCategories)
        }
    }
}

fun List<Exercise>.groupByCategories() = groupBy { it.category }.map { (categories, exercises) ->
    GroupedExercisesViewModel(
        header = categories.fold(emptyString()) { acc, value -> "$acc${value.name} " },
        exercises = exercises.map {
            it.toView()
        }
    )
}

@ExperimentalCoroutinesApi
val App = functionalComponent<RProps> { _ ->
    val (state, setState) = useState<State<ExercisesListViewState>>(defaultState)

    useEffect(emptyList()) {
        scope.launch {
            exerciseListStore.sendAction(ExercisesListActions.Initialization)
            exerciseListStore.state.collect {
                setState(it)
            }
        }
    }

    when (state) {
        is State.Data -> {
            div {
                div {
                    h1 { +"Filtry" }
                    h3 { +"Kategorie" }
                    state.data.categories.forEach { categoryCheckbox ->
                        child(Checkbox) {
                            this.attrs.state = CheckboxModel(
                                viewId = "category_${categoryCheckbox.category.id}",
                                label = categoryCheckbox.category.name,
                                isChecked = categoryCheckbox.isChecked
                            )
                            this.attrs.onCheckedChange = {
                                exerciseListStore.sendAction(
                                    ExercisesListActions.OnCategoryCheckedChange(
                                        categoryId = categoryCheckbox.category.id
                                    )
                                )
                            }
                        }
                    }
                }
                div {
                    h1 { +"Lista ćwiczeń" }

                    state.data.groupedExercises.forEach { (categories, exercises) ->
                        h1 { +categories }
                        exercises.forEach { exercise ->
                            child(ExercisesView) {
                                this.attrs.exercise = exercise
                            }
                        }
                    }
                }
            }
        }
        is State.Loading -> {
            h3 { +"Loading" }
        }
        is State.Error -> {
            h3 { +"Error: ${state.message}" }
        }
    }
}

data class GroupedExercisesViewModel(
    val header: String,
    val exercises: List<ExerciseViewModel>
)

val ExercisesView = functionalComponent<ExercisesViewProps> { props ->
    val (exercise, _) = useState(props.exercise)

    val imageUrl = exercise.animationUrl ?: exercise.imageUrl

    div {
        h3 { +exercise.name }
        imageUrl?.let { animationUrl -> img { attrs.src = animationUrl } }
    }
}

val Checkbox = functionalComponent<CheckboxProps> { props ->
    val (state, _) = useState(props.state)

    div {
        input(type = InputType.checkBox) {
            attrs {
                checked = state.isChecked
                id = state.viewId
                value = state.label
                onChangeFunction = {
                    props.onCheckedChange()
                }
            }
        }
        label {
            +state.label
            attrs["htmlFor"] = state.viewId
        }
    }
}

external interface ExercisesViewProps : RProps {
    var exercise: ExerciseViewModel
}

external interface CheckboxProps : RProps {
    var state: CheckboxModel
    var onCheckedChange: () -> Unit
}

data class CheckboxModel(
    val viewId: String,
    val label: String,
    val isChecked: Boolean
)

sealed class State<T> {

    data class Data<T>(val data: T) : State<T>()
    data class Loading<T>(val data: T? = null) : State<T>()
    data class Error<T>(val message: String) : State<T>()
}

sealed class Resource<T> {

    data class Content<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String) : Resource<T>()
}

data class ExercisesData(
    val exercises: List<Exercise>,
    val categories: List<Category>,
    val equipment: List<Equipment>,
)

fun Exercise.toView() = ExerciseViewModel(
    name = name,
    animationUrl = animationUrl,
    imageUrl = imagesUrls?.firstOrNull(),
    category = category
)

data class ExercisesListViewState(
    val exercises: List<Exercise>,
    val groupedExercises: List<GroupedExercisesViewModel>,
    val categories: List<CategoryCheckboxViewModel>,
    val equipment: List<Equipment>
)

data class CategoryCheckboxViewModel(
    val category: Category,
    val isChecked: Boolean
)

data class ExerciseViewModel(
    val name: String,
    val animationUrl: String?,
    val imageUrl: String?,
    val category: List<Category>
)

@ExperimentalCoroutinesApi
class Store<Type, Action : Any>(defaultState: State<Type>) {

    private val viewModelScope = MainScope()

    var intents = mapOf<KClass<out Action>, Intent<State<Type>>>()

    private val _state = MutableStateFlow(defaultState)

    val state: Flow<State<Type>>
        get() = _state

    fun sendAction(action: Action) {
        viewModelScope.launch {
            val intent = intents[action::class]
            requireNotNull(intent)

            val result = intent.onTrigger?.invoke()

            val newState = intent.reducer(action, result, _state.value)

            _state.value = newState

            intent.sideEffect?.invoke(result, _state.value)
        }
    }
}

sealed class ExercisesListActions {
    object Initialization : ExercisesListActions()
    class OnCategoryCheckedChange(val categoryId: String) : ExercisesListActions()
}

data class Intent<State>(
    val onTrigger: (suspend () -> Any?)?,
    val reducer: suspend (Any, Any?, State) -> State,
    val sideEffect: ((Any?, State) -> Unit)?
)

inline fun <STATE, reified ACTION, reified ACTION_RESULT> intent(
    noinline onTrigger: (suspend () -> ACTION_RESULT?)? = null,
    crossinline reducer: suspend (ACTION, ACTION_RESULT?, STATE) -> STATE,
    noinline sideEffect: ((ACTION_RESULT?, STATE) -> Unit)? = null
) = Intent(
    onTrigger = { onTrigger?.invoke() },
    reducer = { action: Any, actionResult: Any?, currentState: STATE ->
        val typedActionResult = actionResult as? ACTION_RESULT
        reducer(action as ACTION, typedActionResult, currentState)
    },
    sideEffect = { actionResult: Any?, currentState: STATE ->
        val typedActionResult = actionResult as? ACTION_RESULT
        sideEffect?.invoke(typedActionResult, currentState)
    }
)