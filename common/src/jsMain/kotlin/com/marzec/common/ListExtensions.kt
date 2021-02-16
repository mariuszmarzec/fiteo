package com.marzec.common

import com.marzec.extensions.emptyString
import com.marzec.model.domain.Exercise
import com.marzec.screen.exerciselist.model.GroupedExercisesViewModel

fun List<Exercise>.filterByCategoriesAndEquipment(selectedCategoriesIds: Set<String>): List<Exercise> {
    return if (selectedCategoriesIds.isEmpty()) {
        this
    } else {
        filter { exercise ->
            val exercisesCategoryIds = exercise.category.map { it.id }
            val exercisesEquipmentIds = exercise.neededEquipment.map { it.id }
            (exercisesCategoryIds + exercisesEquipmentIds).containsAll(selectedCategoriesIds.toList())
        }
    }
}

fun List<Exercise>.groupByCategories() = groupBy { it.category }.map { (categories, exercises) ->
    GroupedExercisesViewModel(
        header = categories.fold(emptyString()) { acc, value -> "$acc${value.name} " },
        exercises = exercises
    )
}