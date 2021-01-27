package com.marzec

import com.marzec.model.domain.Exercise
import com.marzec.model.dto.CategoryDto
import com.marzec.model.dto.ExerciseDto
import com.marzec.model.domain.Category
import com.marzec.model.domain.Equipment
import com.marzec.model.dto.EquipmentDto
import com.marzec.model.dto.toDomain
import io.ktor.client.request.get
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
import kotlinx.html.label
import react.RProps
import react.child
import react.dom.div
import react.dom.h1
import react.dom.h3
import react.dom.img
import react.dom.input
import react.dom.label
import react.dom.render
import react.dom.ul
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
            ExercisesListActions.Initialization to Intent(
                    onTrigger = {
                        ExercisesData(
                                getExercises(),
                                getCategories(),
                                getEquipment()
                        )
                    },
                    reducer = { actionResult: Any?, _: State<ExercisesListViewState> ->
                        (actionResult as? ExercisesData)?.let { exercisesData ->
                            State.Data(
                                    ExercisesListViewState(
                                            exercises = exercisesData.exercises,
                                            categories = exercisesData.categories,
                                            equipment = exercisesData.equipment
                                    )
                            )
                        } ?: State.Error("Data loading error")
                    },
                    sideEffect = { _: Any?, _: State<ExercisesListViewState> ->
                        console.log("Data loaded!")
                    }
            )
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
                    state.data.categories.forEach { category ->
                        div {
                            input(type = InputType.checkBox) {
                                attrs {
                                    id = "category_${category.id}"
                                    value = category.name
                                }
                            }
                            label {
                                +category.name
                                attrs["for"] = "category_${category.id}"
                            }
                        }
                    }
                }
                div {
                    h1 { +"Lista ćwiczeń" }
                    state.data.exercises.groupBy { it.category }.forEach { (categories, exercises) ->
                        h1 { +categories.fold("") { acc, value -> "$acc${value.name} " } }
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

val ExercisesView = functionalComponent<ExercisesViewProps> { props ->
    val (exercise, _) = useState(props.exercise)

    div {
        h3 { +exercise.name }
        exercise.animationUrl?.let { animationUrl -> img { attrs.src = animationUrl } }
    }
}

external interface ExercisesViewProps : RProps {
    var exercise: Exercise
}

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

data class ExercisesListViewState(
        val exercises: List<Exercise>,
        val categories: List<Category>,
        val equipment: List<Equipment>
)

@ExperimentalCoroutinesApi
class Store<Type, Action>(defaultState: State<Type>) {

    private val viewModelScope = MainScope()

    var intents = mapOf<Action, Intent<State<Type>>>()

    private val _state = MutableStateFlow(defaultState)

    val state: Flow<State<Type>>
        get() = _state

    fun sendAction(action: Action) {
        viewModelScope.launch {
            val intent = intents[action]
            requireNotNull(intent)

            val result = intent.onTrigger.invoke()

            val newState = intent.reducer(result, _state.value)

            _state.value = newState

            intent.sideEffect?.invoke(result, _state.value)
        }
    }
}

sealed class ExercisesListActions {
    object Initialization : ExercisesListActions()
}

data class Intent<State>(
        val onTrigger: suspend () -> Any?,
        val reducer: suspend (Any?, State) -> State,
        val sideEffect: ((Any?, State) -> Unit)?
)