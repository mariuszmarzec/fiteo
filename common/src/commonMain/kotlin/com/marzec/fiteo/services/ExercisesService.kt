package com.marzec.fiteo.services

import com.marzec.fiteo.model.domain.Category
import com.marzec.fiteo.model.domain.Equipment
import com.marzec.fiteo.model.domain.Exercise
import com.marzec.fiteo.repositories.CategoriesRepository
import com.marzec.fiteo.repositories.EquipmentRepository
import com.marzec.fiteo.repositories.ExercisesRepository

interface ExercisesService {
    fun getExercises(): List<Exercise>
    fun getCategories(): List<Category>
    fun getEquipment(): List<Equipment>
}

class ExercisesServiceImpl(
        private val exercisesRepository: ExercisesRepository,
        private val categoriesRepository: CategoriesRepository,
        private val equipmentRepository: EquipmentRepository
) : ExercisesService {

    override fun getExercises(): List<Exercise> {
        return exercisesRepository.getAll()
    }

    override fun getCategories(): List<Category> {
        return categoriesRepository.getAll()
    }

    override fun getEquipment(): List<Equipment> {
        return equipmentRepository.getAll()
    }
}