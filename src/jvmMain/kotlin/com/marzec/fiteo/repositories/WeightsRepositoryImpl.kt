package com.marzec.fiteo.repositories

import com.marzec.cheatday.WeightsRepository
import com.marzec.cheatday.db.WeightEntity
import com.marzec.cheatday.db.WeightsTable
import com.marzec.cheatday.domain.CreateWeight
import com.marzec.cheatday.domain.UpdateWeight
import com.marzec.cheatday.domain.Weight
import com.marzec.core.entity.WithUserEntity
import com.marzec.core.entity.WithUserEntityClass
import com.marzec.core.repository.CommonWithUserRepository
import com.marzec.database.UserEntity
import com.marzec.database.dbCall
import com.marzec.extensions.update
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll

class WeightsRepositoryImpl(
    private val repository: CommonWithUserRepository<Weight, CreateWeight, UpdateWeight>,
    private val database: Database
) : WeightsRepository, CommonWithUserRepository<Weight, CreateWeight, UpdateWeight> by repository {

    override fun getAll(userId: Int): List<Weight> {
        return database.dbCall {
            WeightsTable.selectAll()
                .orderBy(WeightsTable.date to SortOrder.DESC)
                .andWhere { WeightsTable.userId.eq(userId) }.map {
                    WeightEntity.wrapRow(it).toDomain()
                }
        }
    }
}
