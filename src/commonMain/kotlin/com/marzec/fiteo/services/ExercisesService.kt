package com.marzec.fiteo.services

import com.marzec.fiteo.model.domain.*
import com.marzec.fiteo.repositories.CategoriesRepository
import com.marzec.fiteo.repositories.EquipmentRepository
import com.marzec.fiteo.repositories.ExercisesRepository

interface ExercisesService {

    fun getExercises(): List<Exercise>
    fun createExercise(exercise: CreateExercise): Exercise
    fun updateExercise(id: Int, exercise: UpdateExercise): Exercise
    fun getExercise(id: Int): Exercise
    fun deleteExercise(id: Int): Exercise
    fun getCategories(): List<Category>
    fun createCategory(category: Category): Category
    fun updateCategory(id: String, update: UpdateCategory): Category
    fun deleteCategory(id: String): Category
    fun getEquipment(): List<Equipment>
    fun createEquipment(equipment: Equipment): Equipment
    fun updateEquipment(id: String, update: UpdateEquipment): Equipment
    fun deleteEquipment(id: String): Equipment
}

class ExercisesServiceImpl(
        private val exercisesRepository: ExercisesRepository,
        private val categoriesRepository: CategoriesRepository,
        private val equipmentRepository: EquipmentRepository
) : ExercisesService {

    override fun getExercises(): List<Exercise> = exercisesRepository.getAll()

    override fun getCategories(): List<Category> = categoriesRepository.getAll()
    override fun createCategory(category: Category): Category = categoriesRepository.create(category)

    override fun updateCategory(id: String, update: UpdateCategory): Category = categoriesRepository.update(id, update)

    override fun deleteCategory(id: String): Category = categoriesRepository.delete(id)

    override fun getEquipment(): List<Equipment> = equipmentRepository.getAll()
    override fun createEquipment(equipment: Equipment): Equipment = equipmentRepository.create(equipment)

    override fun updateEquipment(id: String, update: UpdateEquipment): Equipment = equipmentRepository.update(id, update)

    override fun deleteEquipment(id: String): Equipment = equipmentRepository.delete(id)

    override fun createExercise(exercise: CreateExercise): Exercise = exercisesRepository.createExercise(exercise)

    override fun updateExercise(id: Int, exercise: UpdateExercise): Exercise = exercisesRepository.updateExercise(id, exercise)

    override fun getExercise(id: Int): Exercise = exercisesRepository.getExercise(id)

    override fun deleteExercise(id: Int): Exercise = exercisesRepository.deleteExercise(id)
}
