package com.marzec.cheatday.db

import com.marzec.cheatday.domain.Weight
import com.marzec.database.IntEntityWithUser
import com.marzec.database.UserEntity
import com.marzec.database.UserTable
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.`java-time`.datetime

object WeightsTable : IntIdTable("weights") {
    val value = float("value")
    val date = datetime("date")
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
}

class WeightEntity(id: EntityID<Int>) : IntEntityWithUser(id) {
    var value by WeightsTable.value
    var date by WeightsTable.date
    override var user: UserEntity by UserEntity referencedOn WeightsTable.userId

    fun toDomain() = Weight(
            id = id.value,
            date = date.toKotlinLocalDateTime(),
            value = value
    )

    companion object : IntEntityClass<WeightEntity>(WeightsTable)
}
