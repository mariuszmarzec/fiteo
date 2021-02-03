package com.marzec.screen.exerciselist.model

import com.marzec.model.domain.Category
import com.marzec.model.domain.Equipment
import com.marzec.model.domain.Exercise
import com.marzec.widget.exerciseview.ExerciseViewModel

data class ExercisesListViewState(
    val exercises: List<Exercise>,
    val groupedExercises: List<GroupedExercisesViewModel>,
    val categories: List<CategoryCheckboxViewModel>,
    val equipment: List<Equipment>
)

data class GroupedExercisesViewModel(
    val header: String,
    val exercises: List<ExerciseViewModel>
)

data class CategoryCheckboxViewModel(
    val category: Category,
    val isChecked: Boolean
)