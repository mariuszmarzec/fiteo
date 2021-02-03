package com.marzec.api.model

import com.marzec.model.domain.Category
import com.marzec.model.domain.Equipment
import com.marzec.model.domain.Exercise

data class ExercisesData(
    val exercises: List<Exercise>,
    val categories: List<Category>,
    val equipment: List<Equipment>,
)