package com.marzec.screen.exerciselist.model

sealed class ExercisesListActions {
    object Initialization : ExercisesListActions()
    class OnCategoryCheckedChange(val categoryId: String) : ExercisesListActions()
}