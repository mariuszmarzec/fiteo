package com.marzec.common

import com.marzec.extensions.emptyString
import com.marzec.fiteo.model.domain.Category
import com.marzec.fiteo.model.domain.Equipment
import com.marzec.fiteo.model.domain.Exercise
import com.marzec.screen.exerciselist.model.GroupedExercisesViewModel

fun List<Exercise>.filter(
    selectedFiltersIds: Set<String>,
    availableCategories: List<Category>,
    availableEquipment: List<Equipment>,
    searchText: String
): List<Exercise> {
    return if (selectedFiltersIds.isEmpty()) {
        this
    } else {
        this@filter.filter { exercise ->
            val exercisesCategoryIds = exercise.category.map { it.id }
            val exercisesEquipmentIds = exercise.neededEquipment.map { it.id }

            val selectedCategoriesIds = availableCategories.filter { it.id in selectedFiltersIds }
                    .ifEmpty { availableCategories }
                    .map { it.id }
            val selectedEquipmentIds = availableEquipment.filter { it.id in selectedFiltersIds }
                    .ifEmpty { availableEquipment }
                    .map { it.id }

            val hasNeededEquipment = selectedEquipmentIds.containsAll(exercisesEquipmentIds)
            val isInCategories = selectedCategoriesIds.containsAll(exercisesCategoryIds)
            hasNeededEquipment && isInCategories
        }
    }.filter { exercise ->
        searchText.split(" ").all { it in exercise.name }
    }
}

fun List<Exercise>.groupByCategories() = groupBy { it.category }.map { (categories, exercises) ->
    GroupedExercisesViewModel(
        header = categories.fold(emptyString()) { acc, value -> "$acc${value.name} " },
        exercises = exercises
    )
}
