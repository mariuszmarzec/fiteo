package com.marzec.common

import com.marzec.extensions.emptyString
import com.marzec.model.domain.Exercise
import com.marzec.screen.exerciselist.model.CategoryCheckboxViewModel
import com.marzec.screen.exerciselist.model.GroupedExercisesViewModel
import com.marzec.widget.exerciseview.toView

fun List<Exercise>.filterByCategories(categories: List<CategoryCheckboxViewModel>): List<Exercise> {
    return if (categories.all { !it.isChecked }) {
        this
    } else {
        val targetCategories = categories.filter { it.isChecked }.map { it.category.id }
        filter { exercise ->
            val exercisesCategory = exercise.category.map { it.id }
            exercisesCategory.containsAll(targetCategories)
        }
    }
}

fun List<Exercise>.groupByCategories() = groupBy { it.category }.map { (categories, exercises) ->
    GroupedExercisesViewModel(
        header = categories.fold(emptyString()) { acc, value -> "$acc${value.name} " },
        exercises = exercises.map {
            it.toView()
        }
    )
}