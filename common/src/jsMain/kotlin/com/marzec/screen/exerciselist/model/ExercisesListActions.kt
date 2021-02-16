package com.marzec.screen.exerciselist.model

sealed class ExercisesListActions {
    object Initialization : ExercisesListActions()
    class OnFilterCheckedChange(val filterId: String) : ExercisesListActions()
}