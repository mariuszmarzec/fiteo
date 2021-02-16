package com.marzec.screen.exerciselist.model

import com.marzec.common.filterByCategoriesAndEquipment
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
                    state.data.categories.forEach { category ->
                        add(
                            CheckboxViewItem(
                                id = category.id,
                                label = category.name,
                                isChecked = category.id in state.data.checkedFilters
                            )
                        )
                    }
                    add(MediumHeaderViewItem(message = "Sprzęt"))
                    state.data.equipment.forEach { equipment ->
                        add(
                            CheckboxViewItem(
                                id = equipment.id,
                                label = equipment.name,
                                isChecked = equipment.id in state.data.checkedFilters
                            )
                        )
                    }
                    add(BigHeaderViewItem(message = "Lista ćwiczeń"))

                    state.data.exercises
                        .filterByCategoriesAndEquipment(state.data.checkedFilters)
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
