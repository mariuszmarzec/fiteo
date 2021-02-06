package com.marzec.screen.exerciselist.model

import com.marzec.common.filterByCategories
import com.marzec.common.groupByCategories
import com.marzec.mvi.State
import com.marzec.views.BigHeaderViewItem
import com.marzec.views.MediumHeaderViewItem
import com.marzec.views.base.ViewItem
import com.marzec.views.checkbox.CheckboxViewItem
import com.marzec.views.error.ErrorItemView
import com.marzec.views.exerciserowview.toView
import com.marzec.views.loading.LoadingItemView

object ExerciseListUiMapper {

    fun map(state: State<ExercisesListViewState>): List<ViewItem> =
        when (state) {
            is State.Data -> {
                mutableListOf<ViewItem>().apply {
                    add(BigHeaderViewItem(message = "Filtry"))
                    add(MediumHeaderViewItem(message = "Kategorie"))
                    state.data.categories.forEach { checkbox ->
                        add(
                            CheckboxViewItem(
                                id = checkbox.id,
                                label = checkbox.name,
                                isChecked = checkbox.id in state.data.checkedCategories
                            )
                        )
                    }
                    add(BigHeaderViewItem(message = "Lista ćwiczeń"))

                    state.data.exercises
                        .filterByCategories(state.data.checkedCategories)
                        .groupByCategories()
                        .forEach { (categories, exercises) ->
                            add(MediumHeaderViewItem(message = categories))

                            exercises.forEach { exercise ->
                                add(exercise.toView())
                            }
                        }

                }
            }
            is State.Loading -> {
                listOf(LoadingItemView())
            }
            is State.Error -> {
                listOf(ErrorItemView(message = state.message))
            }
        }
}
