package com.marzec.fiteo.repositories

import com.marzec.fiteo.model.domain.Exercise

interface ExercisesRepository {
    fun getAll(): List<Exercise>
    fun addAll(exercises: List<Exercise>)
}
