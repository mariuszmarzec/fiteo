package com.marzec.screen.exerciselist.model

import com.marzec.api.getCategories
import com.marzec.api.getEquipment
import com.marzec.api.getExercises
import com.marzec.api.model.ExercisesData
import com.marzec.common.filterByCategories
import com.marzec.common.groupByCategories
import com.marzec.extensions.replaceIf
import com.marzec.mvi.State
import com.marzec.mvi.Store
import com.marzec.mvi.intent
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