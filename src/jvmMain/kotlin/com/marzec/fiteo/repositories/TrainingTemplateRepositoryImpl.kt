package com.marzec.fiteo.repositories

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
import com.marzec.fiteo.model.domain.CreateTrainingTemplate
import com.marzec.fiteo.model.domain.CreateTrainingTemplatePart
import com.marzec.fiteo.model.domain.TrainingTemplate
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll

class TrainingTemplateRepositoryImpl(private val database: Database) : TrainingTemplateRepository {

    override fun getTemplates(userId: Int): List<TrainingTemplate> = database.dbCall {
        TrainingTemplateTable.selectAll().andWhere {
            TrainingTemplateTable.userId.eq(userId)
        }.map { TrainingTemplateEntity.wrapRow(it).toDomain() }
    }

    override fun getTemplate(userId: Int, templateId: Int): TrainingTemplate = database.dbCall {
        TrainingTemplateTable.selectAll().andWhere {
            TrainingTemplateTable.userId.eq(userId) and TrainingTemplateTable.id.eq(templateId)
        }.map { TrainingTemplateEntity.wrapRow(it).toDomain() }.first()
    }

    override fun addTemplate(userId: Int, trainingTemplate: CreateTrainingTemplate): TrainingTemplate {

        val trainingParts = trainingTemplate.exercises.map { templatePart ->
            addTemplatePart(templatePart)
        }

        val availableEquipment = database.dbCall {
            trainingTemplate.availableEquipmentIds.map {
                EquipmentEntity.findByIdOrThrow(it)
            }
        }

        val templateEntity = database.dbCall {
            TrainingTemplateEntity.new {
                name = trainingTemplate.name
                user = UserEntity[userId]
            }
        }

        return database.dbCall {
            templateEntity.parts = trainingParts.toSized()
            templateEntity.availableEquipment = availableEquipment.toSized()
            templateEntity.toDomain()
        }
    }


    override fun updateTemplate(userId: Int, trainingTemplate: CreateTrainingTemplate): TrainingTemplate =
        database.dbCall {
            val parts = trainingTemplate.exercises.map { addTemplatePart(it) }

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

    override fun removeTemplate(userId: Int, trainingTemplateId: Int): TrainingTemplate = database.dbCall {
        val templateEntity = TrainingTemplateEntity.findByIdOrThrow(trainingTemplateId)
        val trainingTemplate = templateEntity.toDomain()
        templateEntity.deleteIfBelongsToUserOrThrow(userId)
        trainingTemplate
    }

    private fun addTemplatePart(templatePart: CreateTrainingTemplatePart): TrainingTemplatePartEntity {
        val partEntity = database.dbCall {
            TrainingTemplatePartEntity.new {
                this.name = templatePart.name
            }
        }
        return database.dbCall {
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
