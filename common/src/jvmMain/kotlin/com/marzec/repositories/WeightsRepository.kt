package com.marzec.repositories

import com.marzec.cheatday.WeightsRepository
import com.marzec.cheatday.db.WeightEntity
import com.marzec.cheatday.db.WeightsTable
import com.marzec.cheatday.domain.Weight
import com.marzec.database.UserEntity
import com.marzec.database.dbCall
import com.marzec.database.findByIdOrThrow
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll

class WeightsRepositoryImpl: WeightsRepository {

    override fun getWeights(userId: Int): List<Weight> {
        return dbCall {
            WeightsTable.selectAll().andWhere { WeightsTable.userId.eq(userId) }.map {
                WeightEntity.wrapRow(it).toDomain()
            }
        }
    }

    override fun addWeight(userId: Int, weight: Float, date: LocalDateTime): Weight {
        return dbCall {
            WeightEntity.new {
                this.value = weight
                this.date = date.toJavaLocalDateTime()
                this.user = UserEntity.findById(userId)!!
            }
        }.toDomain()
    }

    override fun removeWeight(userId: Int, weightId: Int): Weight {
        return dbCall {
            val weightEntity = WeightEntity.findByIdOrThrow(weightId)
            weightEntity.belongsToUserOrThrow(userId)
            weightEntity.delete()
            weightEntity.toDomain()
        }
    }

    override fun updateWeight(userId: Int, weight: Weight): Weight {
        return dbCall {
            val weightEntity = WeightEntity.findByIdOrThrow(weight.id)
            weightEntity.belongsToUserOrThrow(userId)
            weightEntity.date = weight.date.toJavaLocalDateTime()
            weightEntity.value = weight.value
            weightEntity.toDomain()
        }
    }
}