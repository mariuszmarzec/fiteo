package com.marzec.fiteo.repositories

import com.marzec.database.*
import com.marzec.fiteo.model.domain.CreateTrainingTemplate
import com.marzec.fiteo.model.domain.CreateTrainingTemplatePart
import com.marzec.fiteo.model.domain.TrainingTemplate
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.*

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

        val trainingParts = trainingTemplate.exercises.mapIndexed { index, templatePart ->
            addTemplatePart(index, templatePart)
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
            val templateEntity = TrainingTemplateEntity.findByIdOrThrow(trainingTemplate.id).load(
                TrainingTemplateEntity::parts,
                TrainingTemplateEntity::availableEquipment,
            )
            templateEntity.belongsToUserOrThrow(userId)

            val newParts = trainingTemplate.exercises.mapIndexed { index, part ->
                updateExistedOrCreate(trainingTemplate.id, part, index)
            }

            removePartsIfNotPresentInNewOnes(newParts, templateEntity)

            templateEntity.name = trainingTemplate.name
            templateEntity.parts = newParts.toSized()
            templateEntity.availableEquipment = trainingTemplate.availableEquipmentIds.map {
                EquipmentEntity.findByIdOrThrow(it)
            }.toSized()

            templateEntity.toDomain()
        }

    private fun removePartsIfNotPresentInNewOnes(
        newParts: List<TrainingTemplatePartEntity>,
        templateEntity: TrainingTemplateEntity
    ) {
        val newPartsIds = newParts.map { it.id.value }
        val partsToRemove = templateEntity.parts.filterNot { it.id.value in newPartsIds }
        partsToRemove.forEach { it.delete() }
    }

    private fun updateExistedOrCreate(
        trainingTemplateId: Int,
        part: CreateTrainingTemplatePart,
        index: Int
    ) = part.id?.let {
        TrainingTemplatePartEntity.findById(it)?.apply {
            if (trainingTemplate.first().id.value != trainingTemplateId) {
                throw IllegalAccessException("Part with $id belongs to different training")
            }
            name = part.name
            ordinalNumber = index
            updatedSized(part)
        }
    } ?: addTemplatePart(index, part)

    override fun removeTemplate(userId: Int, trainingTemplateId: Int): TrainingTemplate = database.dbCall {
        val templateEntity = TrainingTemplateEntity.findByIdOrThrow(trainingTemplateId)
        val trainingTemplate = templateEntity.toDomain()
        templateEntity.deleteIfBelongsToUserOrThrow(userId)
        trainingTemplate
    }

    private fun addTemplatePart(ordinalNumber: Int, templatePart: CreateTrainingTemplatePart): TrainingTemplatePartEntity {
        val partEntity = database.dbCall {
            TrainingTemplatePartEntity.new {
                this.name = templatePart.name
                this.ordinalNumber = ordinalNumber
            }
        }
        return database.dbCall {
            partEntity.apply {
                updatedSized(templatePart)
            }
        }
    }

    private fun TrainingTemplatePartEntity.updatedSized(templatePart: CreateTrainingTemplatePart) {
        val categories = templatePart.categoryIds.map { CategoryEntity.findByIdOrThrow(it) }
        val excludedEquipment = templatePart.excludedEquipmentIds.map { EquipmentEntity.findByIdOrThrow(it) }
        val excludedExercises = templatePart.excludedExercisesIds.map { ExerciseEntity.findByIdOrThrow(it) }

        this.pinnedExercise = templatePart.pinnedExerciseId?.let { ExerciseEntity.findById(it) }
        this.categories = categories.toSized()
        this.excludedEquipment = excludedEquipment.toSized()
        this.excludedExercises = excludedExercises.toSized()
    }
}
