package com.marzec.repositories

import com.marzec.database.CategoryEntity
import com.marzec.database.EquipmentEntity
import com.marzec.database.ExerciseEntity
import com.marzec.database.TrainingTemplateEntity
import com.marzec.database.TrainingTemplatePartEntity
import com.marzec.database.TrainingTemplateTable
import com.marzec.database.UserEntity
import com.marzec.database.dbCall
import com.marzec.database.findByIdOrThrow
import com.marzec.database.toSized
import com.marzec.model.domain.CreateTrainingTemplate
import com.marzec.model.domain.CreateTrainingTemplatePart
import com.marzec.model.domain.TrainingTemplate
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll

class TrainingTemplateRepositoryImpl : TrainingTemplateRepository {

    override fun getTemplates(userId: Int): List<TrainingTemplate> = dbCall {
        TrainingTemplateTable.selectAll().andWhere {
            TrainingTemplateTable.userId.eq(userId)
        }.map { TrainingTemplateEntity.wrapRow(it).toDomain() }
    }

    override fun addTemplate(userId: Int, trainingTemplate: CreateTrainingTemplate): TrainingTemplate {

        val trainingParts = trainingTemplate.exercises.map { templatePart ->
            addTemplatePart(templatePart)
        }

        val availableEquipment = dbCall {
            trainingTemplate.availableEquipmentIds.map {
                EquipmentEntity.findByIdOrThrow(it)
            }
        }

        val templateEntity = dbCall {
            TrainingTemplateEntity.new {
                name = trainingTemplate.name
                user = UserEntity[userId]
            }
        }

        return dbCall {
            templateEntity.parts = trainingParts.toSized()
            templateEntity.availableEquipment = availableEquipment.toSized()
            templateEntity.toDomain()
        }
    }


    override fun updateTemplate(userId: Int, trainingTemplate: CreateTrainingTemplate): TrainingTemplate {
        val parts = trainingTemplate.exercises.map { addTemplatePart(it) }
        return dbCall {
            val templateEntity = TrainingTemplateEntity.findByIdOrThrow(trainingTemplate.id).load(
                    TrainingTemplateEntity::parts,
                    TrainingTemplateEntity::availableEquipment,
            )
            templateEntity.belongsToUserOrThrow(userId)

            templateEntity.name = trainingTemplate.name

            templateEntity.parts.forEach { it.delete() }
            templateEntity.parts = parts.toSized()

            templateEntity.availableEquipment = trainingTemplate.availableEquipmentIds.map {
                EquipmentEntity.findByIdOrThrow(it)
            }.toSized()

            templateEntity.toDomain()
        }
    }

    override fun removeTemplate(userId: Int, trainingTemplateId: Int): TrainingTemplate = dbCall {
        val templateEntity = TrainingTemplateEntity.findByIdOrThrow(trainingTemplateId)
        templateEntity.deleteIfBelongsToUserOrThrow(userId)
        templateEntity.toDomain()
    }

    private fun addTemplatePart(templatePart: CreateTrainingTemplatePart): TrainingTemplatePartEntity {
        val partEntity = dbCall {
            TrainingTemplatePartEntity.new {
                this.name = templatePart.name
            }
        }
        return dbCall {
            partEntity.apply {
                val categories = templatePart.categoryIds.map { CategoryEntity.findByIdOrThrow(it) }
                val excludedEquipment = templatePart.excludedEquipmentIds.map { EquipmentEntity.findByIdOrThrow(it) }
                val excludedExercises = templatePart.excludedExercisesIds.map { ExerciseEntity.findByIdOrThrow(it) }

                this.pinnedExercise = templatePart.pinnedExerciseId?.let { ExerciseEntity.findById(it) }
                this.categories = categories.toSized()
                this.excludedEquipment = excludedEquipment.toSized()
                this.excludedExercises = excludedExercises.toSized()
            }
        }
    }
}