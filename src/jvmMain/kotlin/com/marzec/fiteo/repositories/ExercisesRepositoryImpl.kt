package com.marzec.fiteo.repositories

import com.marzec.database.CategoryEntity
import com.marzec.database.CategoryTable
import com.marzec.database.EquipmentEntity
import com.marzec.database.EquipmentTable
import com.marzec.database.ExerciseEntity
import com.marzec.database.dbCall
import com.marzec.fiteo.model.domain.Exercise
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.insertAndGetId

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
}