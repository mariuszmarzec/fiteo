package com.marzec.repositories

import com.marzec.cheatday.WeightsRepository
import com.marzec.cheatday.db.WeightEntity
import com.marzec.cheatday.db.WeightsTable
import com.marzec.cheatday.domain.Weight
import com.marzec.database.dbCall
import kotlinx.datetime.toKotlinLocalDateTime
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
}