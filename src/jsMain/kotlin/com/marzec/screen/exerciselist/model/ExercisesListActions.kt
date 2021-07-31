package com.marzec.screen.exerciselist.model

sealed class ExercisesListActions {
    class Initialization(val query: String, val filters: Set<String>) : ExercisesListActions()
    class OnFilterCheckedChange(val filterId: String) : ExercisesListActions()
    class OnSearchTextChanged(val text: String) : ExercisesListActions()
}