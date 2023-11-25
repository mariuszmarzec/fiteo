package com.marzec.fiteo.repositories

import com.marzec.cheatday.WeightsRepository
import com.marzec.cheatday.db.WeightEntity
import com.marzec.cheatday.db.WeightsTable
import com.marzec.cheatday.domain.UpdateWeight
import com.marzec.cheatday.domain.Weight
import com.marzec.database.UserEntity
import com.marzec.database.dbCall
import com.marzec.database.findByIdOrThrow
import com.marzec.extensions.update
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll

class WeightsRepositoryImpl(private val database: Database) : WeightsRepository {

    override fun getWeights(userId: Int): List<Weight> {
        return database.dbCall {
            WeightsTable.selectAll()
                .orderBy(WeightsTable.date to SortOrder.DESC)
                .andWhere { WeightsTable.userId.eq(userId) }.map {
                    WeightEntity.wrapRow(it).toDomain()
                }
        }
    }

    override fun getWeight(userId: Int, weightId: Int): Weight = database.dbCall {
        val weightEntity = WeightEntity.findByIdOrThrow(weightId)
        weightEntity.belongsToUserOrThrow(userId)
        weightEntity.toDomain()
    }

    override fun updateWeight(userId: Int, weightId: Int, weight: UpdateWeight): Weight = database.dbCall {
        WeightEntity.findByIdOrThrow(weightId).apply {
            belongsToUserOrThrow(userId)
            update(this::value, weight.value)
            update(this::date, weight.date?.toLocalDateTime()?.toJavaLocalDateTime())
        }.toDomain()
    }

    override fun addWeight(userId: Int, weight: Float, date: LocalDateTime): Weight {
        return database.dbCall {
            WeightEntity.new {
                this.value = weight
                this.date = date.toJavaLocalDateTime()
                this.user = UserEntity.findById(userId)!!
            }
        }.toDomain()
    }

    override fun removeWeight(userId: Int, weightId: Int): Weight {
        return database.dbCall {
            val weightEntity = WeightEntity.findByIdOrThrow(weightId)
            weightEntity.belongsToUserOrThrow(userId)
            weightEntity.delete()
            weightEntity.toDomain()
        }
    }

    override fun updateWeight(userId: Int, weight: Weight): Weight {
        return database.dbCall {
            val weightEntity = WeightEntity.findByIdOrThrow(weight.id)
            weightEntity.belongsToUserOrThrow(userId)
            weightEntity.date = weight.date.toJavaLocalDateTime()
            weightEntity.value = weight.value
            weightEntity.toDomain()
        }
    }
}
