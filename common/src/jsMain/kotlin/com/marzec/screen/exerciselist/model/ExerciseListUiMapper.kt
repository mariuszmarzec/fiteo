package com.marzec.screen.exerciselist.model

import com.marzec.common.filter
import com.marzec.common.groupByCategories
import com.marzec.mvi.State
import com.marzec.views.BigHeaderViewItem
import com.marzec.views.MediumHeaderViewItem
import com.marzec.views.base.ViewItem
import com.marzec.views.checkbox.CheckboxViewItem
import com.marzec.views.error.ErrorItemView
import com.marzec.views.loading.LoadingItemView
import com.marzec.views.textinput.TextInputViewItem
import com.marzec.views.exerciserowview.toView
import com.marzec.views.horizontalsplitview.HorizontalSplitView

object ExerciseListUiMapper {

    fun map(state: State<ExercisesListViewState>): List<ViewItem> =
        when (state) {
            is State.Data -> {
                listOf<ViewItem>(
                    HorizontalSplitView(
                        id = "main",
                        leftColumnItems = mutableListOf<ViewItem>().apply {

                            add(BigHeaderViewItem(message = "Lista ćwiczeń"))

                            state.data.exercises
                                .filter(
                                    state.data.checkedFilters,
                                    state.data.categories,
                                    state.data.equipment,
                                    state.data.searchText
                                )
                                .groupByCategories()
                                .forEach { (categories, exercises) ->
                                    add(MediumHeaderViewItem(message = categories))

                                    exercises.forEach { exercise ->
                                        add(exercise.toView())
                                    }
                                }

                        },
                        rightColumnItems = mutableListOf<ViewItem>().apply {
                            add(TextInputViewItem(
                                id = "SEARCH",
                                text = state.data.searchText,
                                hint = "Szukaj"
                            ))
                            add(BigHeaderViewItem(message = "Filtry"))
                            add(MediumHeaderViewItem(message = "Kategorie"))
                            state.data.categories.forEach { category ->
                                add(
                                    CheckboxViewItem(
                                        id = category.id,
                                        label = category.name,
                                        isChecked = category.id in state.data.checkedFilters
                                    )
                                )
                            }
                            add(MediumHeaderViewItem(message = "Sprzęt (jaki posiadasz)"))
                            state.data.equipment.sortedBy { it.name }.forEach { equipment ->
                                add(
                                    CheckboxViewItem(
                                        id = equipment.id,
                                        label = equipment.name,
                                        isChecked = equipment.id in state.data.checkedFilters
                                    )
                                )
                            }
                        }
                    )
                )
            }
            is State.Loading -> {
                listOf(LoadingItemView())
            }
            is State.Error -> {
                listOf(ErrorItemView(message = state.message))
            }
        }
}
