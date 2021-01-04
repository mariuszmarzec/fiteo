package com.marzec

import com.marzec.model.domain.Exercise
import com.marzec.model.dto.ExerciseDto
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
import react.RProps
import react.child
import react.dom.div
import react.dom.h1
import react.dom.h3
import react.dom.img
import react.dom.render
import react.dom.ul
import react.functionalComponent
import react.useEffect
import react.useState


val endpoint = window.location.origin

suspend fun getExercises(): List<Exercise> =
        jsonClient.get<List<ExerciseDto>>(endpoint + ApiPath.EXERCISES)
                .map { it.toDomain() }

@ExperimentalCoroutinesApi
fun main() {
    render(document.getElementById("root")) {
        child(App)
    }
}

private val scope = MainScope()

val defaultState = State.Loading<List<Exercise>>(emptyList())

@ExperimentalCoroutinesApi
val exerciseListViewModel = ViewModel<List<Exercise>, ExercisesListActions>(defaultState).apply {
    intents = mapOf(
            ExercisesListActions.Initialization to Intent(
                    onTrigger = {
                        getExercises()
                    },
                    reducer = { actionResult: Any?, state: State<List<Exercise>> ->
                        (actionResult as? List<Exercise>)?.let { State.Data(it) } ?: State.Error("Data loading error")
                    }
            )
    )
}

@ExperimentalCoroutinesApi
val App = functionalComponent<RProps> { _ ->
    val (state, setState) = useState<State<List<Exercise>>>(defaultState)

    useEffect(emptyList()) {
        scope.launch {
            exerciseListViewModel.sendAction(ExercisesListActions.Initialization)
            exerciseListViewModel.state.collect {
                setState(it)
            }
        }
    }

    when (state) {
        is State.Data -> {
            ul {
                state.data.groupBy { it.category }.forEach { (categories, exercises) ->
                    h1 { +categories.fold("") { acc, value -> "$acc${value.name} " } }
                    exercises.forEach { exercise ->
                        child(ExercisesView) {
                            this.attrs.exercise = exercise
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
    data class Loading<T>(val data: T?) : State<T>()
    data class Error<T>(val message: String) : State<T>()
}

sealed class Resource<T> {

    data class Content<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String) : Resource<T>()
}

@ExperimentalCoroutinesApi
class ViewModel<Type, Action>(defaultState: State<Type>) {

    private val viewModelScope = MainScope()

    var intents = mapOf<Action, Intent<Action, State<Type>>>()

    private val _state = MutableStateFlow(defaultState)

    val state: Flow<State<Type>>
        get() = _state

    fun sendAction(action: Action) {
        viewModelScope.launch {
            val intent = intents[action]
            console.log(intent)
            requireNotNull(intent)

            val result = intent.onTrigger.invoke()
            console.log(result)

            val newState = intent.reducer(result, _state.value)

            _state.value = newState
        }
    }
}

sealed class ExercisesListActions {
    object Initialization : ExercisesListActions()
}

data class Intent<Action, State>(
        val onTrigger: suspend () -> Any?,
        val reducer: suspend (Any?, State) -> State
)