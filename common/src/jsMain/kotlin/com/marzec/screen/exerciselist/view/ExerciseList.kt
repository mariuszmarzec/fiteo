package com.marzec.screen.exerciselist.view

import com.marzec.common.useStateFlow
import com.marzec.mvi.State
import com.marzec.screen.exerciselist.model.ExercisesListActions
import com.marzec.screen.exerciselist.model.defaultState
import com.marzec.screen.exerciselist.model.exerciseListStore
import com.marzec.widget.checkbox.Checkbox
import com.marzec.widget.checkbox.CheckboxModel
import com.marzec.widget.exerciseview.ExerciseView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import react.RProps
import react.child
import react.dom.div
import react.dom.h1
import react.dom.h3
import react.functionalComponent
import react.key
import react.useEffect

@ExperimentalCoroutinesApi
val ExerciseList = functionalComponent<RProps> { _ ->
    val state = useStateFlow(exerciseListStore.state, defaultState)

    useEffect(emptyList()) {
        exerciseListStore.sendAction(ExercisesListActions.Initialization)
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
                            this.attrs.key = categoryCheckbox.category.id
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
                            child(ExerciseView) {
                                this.attrs.key = exercise.id.toString()
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
