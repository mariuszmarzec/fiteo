package com.marzec.screen.exerciselist.model

import com.marzec.api.getCategories
import com.marzec.api.getEquipment
import com.marzec.api.getExercises
import com.marzec.api.model.ExercisesData
import com.marzec.common.filterByCategories
import com.marzec.common.groupByCategories
import com.marzec.extensions.replaceIf
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
            reducer = { _: ExercisesListActions, actionResult: ExercisesData?, _: State<ExercisesListViewState> ->
                actionResult?.let { exercisesData ->
                    State.Data(
                        ExercisesListViewState(
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
        ExercisesListActions.OnCategoryCheckedChange::class to intent(
            reducer = { action: ExercisesListActions.OnCategoryCheckedChange, _: Any?, currentState: State<ExercisesListViewState> ->
                when (currentState) {
                    is State.Data -> {
                        val checkedCategories = if (action.categoryId in currentState.data.checkedCategories) {
                            currentState.data.checkedCategories.toMutableSet().apply { remove(action.categoryId) }
                        } else {
                            currentState.data.checkedCategories.toMutableSet().apply { add(action.categoryId) }
                        }
                        State.Data(currentState.data.copy(checkedCategories = checkedCategories))
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