package com.marzec.api.model

import com.marzec.fiteo.model.domain.Category
import com.marzec.fiteo.model.domain.Equipment
import com.marzec.fiteo.model.domain.Exercise

data class ExercisesData(
    val exercises: List<Exercise>,
    val categories: List<Category>,
    val equipment: List<Equipment>,
)
