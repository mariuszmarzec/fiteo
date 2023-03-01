package com.marzec.fiteo.services

import com.marzec.fiteo.model.domain.Category
import com.marzec.fiteo.model.domain.CreateExercise
import com.marzec.fiteo.model.domain.Equipment
import com.marzec.fiteo.model.domain.Exercise
import com.marzec.fiteo.repositories.CategoriesRepository
import com.marzec.fiteo.repositories.EquipmentRepository
import com.marzec.fiteo.repositories.ExercisesRepository

interface ExercisesService {

    fun getExercises(): List<Exercise>
    fun getCategories(): List<Category>
    fun getEquipment(): List<Equipment>
    fun createExercise(exercise: CreateExercise): Exercise
}

class ExercisesServiceImpl(
        private val exercisesRepository: ExercisesRepository,
        private val categoriesRepository: CategoriesRepository,
        private val equipmentRepository: EquipmentRepository
) : ExercisesService {

    override fun getExercises(): List<Exercise> = exercisesRepository.getAll()

    override fun getCategories(): List<Category> = categoriesRepository.getAll()

    override fun getEquipment(): List<Equipment> = equipmentRepository.getAll()

    override fun createExercise(exercise: CreateExercise): Exercise = exercisesRepository.createExercise(exercise)
}
