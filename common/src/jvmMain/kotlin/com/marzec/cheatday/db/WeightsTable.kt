package com.marzec.cheatday.db

import com.marzec.database.UserTable
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

class WeightEntity(id: EntityID<Int>) : IntEntity(id) {
    val value by WeightsTable.value
    val date by WeightsTable.date
    val userId by WeightsTable.userId

    companion object : IntEntityClass<WeightEntity>(WeightsTable)
}