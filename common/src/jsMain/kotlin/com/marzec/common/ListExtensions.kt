package com.marzec.common

import com.marzec.extensions.emptyString
import com.marzec.model.domain.Exercise
import com.marzec.screen.exerciselist.model.GroupedExercisesViewModel

fun List<Exercise>.filterByCategories(selectedCategories: Set<String>): List<Exercise> {
    return if (selectedCategories.isEmpty()) {
        this
    } else {
        filter { exercise ->
            val exercisesCategory = exercise.category.map { it.id }
            exercisesCategory.containsAll(selectedCategories)
        }
    }
}

fun List<Exercise>.groupByCategories() = groupBy { it.category }.map { (categories, exercises) ->
    GroupedExercisesViewModel(
        header = categories.fold(emptyString()) { acc, value -> "$acc${value.name} " },
        exercises = exercises
    )
}