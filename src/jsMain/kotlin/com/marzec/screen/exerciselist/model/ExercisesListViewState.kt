package com.marzec.screen.exerciselist.model

import com.marzec.fiteo.model.domain.Category
import com.marzec.fiteo.model.domain.Equipment
import com.marzec.fiteo.model.domain.Exercise

data class ExercisesListViewState(
    val exercises: List<Exercise>,
    val categories: List<Category>,
    val equipment: List<Equipment>,
    val checkedFilters: Set<String> = emptySet(),
    val searchText: String = ""
)

data class GroupedExercisesViewModel(
    val header: String,
    val exercises: List<Exercise>
)
