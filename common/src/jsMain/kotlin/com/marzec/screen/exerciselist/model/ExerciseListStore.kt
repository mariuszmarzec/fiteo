package com.marzec.screen.exerciselist.model

import com.marzec.api.getCategories
import com.marzec.api.getEquipment
import com.marzec.api.getExercises
import com.marzec.api.model.ExercisesData
import com.marzec.extensions.flip
import com.marzec.mvi.Intent
import com.marzec.mvi.State
import com.marzec.mvi.Store
import kotlinx.coroutines.ExperimentalCoroutinesApi

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
            reducer = { action: ExercisesListActions.Initialization, actionResult: ExercisesData?, _: State<ExercisesListViewState> ->
                actionResult?.let { exercisesData ->
                    State.Data(
                        ExercisesListViewState(
                            searchText = action.query,
                            exercises = exercisesData.exercises,
                            categories = exercisesData.categories,
                            equipment = exercisesData.equipment
                        )
                    )
                } ?: State.Error("Data loading error")
            },
            sideEffect = { _: ExercisesData?, _: State<ExercisesListViewState> ->
                console.log("Data loaded!")
            }
        ),
        ExercisesListActions.OnFilterCheckedChange::class to intent(
            reducer = { action: ExercisesListActions.OnFilterCheckedChange, _: Any?, currentState: State<ExercisesListViewState> ->
                when (currentState) {
                    is State.Data -> {
                        val checkedFilters = currentState.data.checkedFilters.flip(action.filterId)
                        State.Data(currentState.data.copy(checkedFilters = checkedFilters))
                    }
                    is State.Loading -> currentState.copy()
                    is State.Error -> currentState.copy()
                }
            }
        ),
        ExercisesListActions.OnSearchTextChanged::class to intent(
            reducer = { action: ExercisesListActions.OnSearchTextChanged, _: Any?, currentState: State<ExercisesListViewState> ->
                when (currentState) {
                    is State.Data -> {
                        State.Data(currentState.data.copy(searchText = action.text))
                    }
                    is State.Loading -> currentState.copy()
                    is State.Error -> currentState.copy()
                }
            }
        )
    )
}

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