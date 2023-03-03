package com.marzec.fiteo.repositories

import com.marzec.database.CategoryEntity
import com.marzec.database.CategoryTable
import com.marzec.database.EquipmentEntity
import com.marzec.database.EquipmentTable
import com.marzec.database.ExerciseEntity
import com.marzec.database.dbCall
import com.marzec.database.findByIdOrThrow
import com.marzec.extensions.update
import com.marzec.extensions.updateNullable
import com.marzec.fiteo.model.domain.CreateExercise
import com.marzec.fiteo.model.domain.Exercise
import com.marzec.fiteo.model.domain.NullableField
import com.marzec.fiteo.model.domain.UpdateExercise
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.emptySized
import org.jetbrains.exposed.sql.insertAndGetId
import kotlin.reflect.KMutableProperty0

class ExercisesRepositoryImpl(private val database: Database) : ExercisesRepository {

    override fun getAll(): List<Exercise> = database.dbCall {
        ExerciseEntity.all().map { it.toDomain() }
    }

    override fun addAll(exercises: List<Exercise>) =
            exercises.forEach { exercise ->
                val equipment = database.dbCall {
                    SizedCollection(
                            exercise.neededEquipment.map { equipment ->
                                EquipmentEntity.findById(equipment.id) ?: EquipmentEntity.findById(
                                        EquipmentTable.insertAndGetId {
                                            it[id] = equipment.id
                                            it[name] = equipment.name
                                        }
                                )!!
                            }
                    )
                }
                val categories = database.dbCall {
                    SizedCollection(
                            exercise.category.map { category ->
                                CategoryEntity.findById(category.id) ?: CategoryEntity.findById(
                                        CategoryTable.insertAndGetId {
                                            it[id] = category.id
                                            it[name] = category.name
                                        }
                                )!!
                            }
                    )
                }

                val exercise = database.dbCall {
                    ExerciseEntity.new {
                        name = exercise.name
                        animationImageName = exercise.animationImageName
                        animationUrl = exercise.animationUrl
                        videoUrl = exercise.videoUrl
                        imagesNames = exercise.imagesNames
                        imagesUrls = exercise.imagesUrls
                        descriptionsToImages = exercise.descriptionsToImages
                        imagesMistakesUrls = exercise.imagesMistakesUrls
                        imagesMistakesNames = exercise.imagesMistakesNames
                        descriptionsToMistakes = exercise.descriptionsToMistakes
                        muscles = exercise.muscles
                        musclesName = exercise.musclesName
                        thumbnailName = exercise.thumbnailName
                        thumbnailUrl = exercise.thumbnailUrl
                    }
                }
                database.dbCall {
                    exercise.category = categories
                    exercise.neededEquipment = equipment
                }
            }

    override fun createExercise(exercise: CreateExercise) = database.dbCall {
        ExerciseEntity.new {
            name = exercise.name
            animationUrl = exercise.animationUrl
            videoUrl = exercise.videoUrl
            thumbnailUrl = exercise.thumbnailUrl
            category = CategoryEntity.forIds(exercise.category.map { it.id })
            neededEquipment = EquipmentEntity.forIds(exercise.neededEquipment.map { it.id })
        }.toDomain()
    }

    override fun getExercise(id: Int): Exercise = database.dbCall {
        ExerciseEntity.findByIdOrThrow(id).toDomain()
    }

    override fun deleteExercise(id: Int): Exercise =database.dbCall {
        val entity = ExerciseEntity.findByIdOrThrow(id)
        val domain = entity.toDomain()
        entity.category = emptySized()
        entity.neededEquipment = emptySized()
        entity.delete()
        domain
    }

    override fun updateExercise(id: Int, update: UpdateExercise): Exercise = database.dbCall {
        ExerciseEntity.findByIdOrThrow(id).apply {
            update(this::name, update.name)
            updateNullable(this::animationUrl, update.animationUrl)
            updateNullable(this::videoUrl, update.videoUrl)
            updateNullable(this::thumbnailUrl, update.thumbnailUrl)
            update(this::category, update.category?.map { it.id }?.let { CategoryEntity.forIds(it) })
            update(this::neededEquipment, update.neededEquipment?.map { it.id }?.let { EquipmentEntity.forIds(it) })
        }.toDomain()
    }
}
