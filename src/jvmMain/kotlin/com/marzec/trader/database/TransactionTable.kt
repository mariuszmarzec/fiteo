package com.marzec.trader.database

import com.marzec.core.toDecimal
import com.marzec.database.IntEntityWithUser
import com.marzec.database.UserEntity
import com.marzec.database.UserTable
import com.marzec.trader.model.Transaction
import com.marzec.trader.model.TransactionType
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime

object TransactionTable : IntIdTable("transactions") {
    val title = varchar("title", 200)
    val date = datetime("date_time")
    val targetPaper = reference("target_paper_id", PapersTable, onDelete = ReferenceOption.RESTRICT)
    val sourcePaper = reference("source_paper_id", PapersTable, onDelete = ReferenceOption.RESTRICT)
    val targetValue = varchar("target_value", 100)
    val totalPriceInSource = varchar("total_price_in_source", 100)
    val pricePerUnit = varchar("price_per_unit", 100)
    val settlementRate = varchar("settlement_rate", 100)
    val fee = varchar("fee", 100)
    val feePaper = reference("fee_paper_id", PapersTable, onDelete = ReferenceOption.RESTRICT)
    val type = varchar("type", 100)
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)

}

class TransactionEntity(id: EntityID<Int>) : IntEntityWithUser(id) {
    var title by TransactionTable.title
    var date by TransactionTable.date
    var targetPaper by PaperEntity referencedOn TransactionTable.targetPaper
    var sourcePaper by PaperEntity referencedOn TransactionTable.sourcePaper
    var targetValue by TransactionTable.targetValue
    var totalPriceInSource by TransactionTable.totalPriceInSource
    var pricePerUnit by TransactionTable.pricePerUnit
    var settlementRate by TransactionTable.settlementRate
    var fee by TransactionTable.fee
    var feePaper by PaperEntity referencedOn TransactionTable.feePaper
    var type by TransactionTable.type

    override var user by UserEntity referencedOn TransactionTable.userId

    fun toDomain() = Transaction(
        id = id.value.toLong(),
        title = title,
        date = date.toKotlinLocalDateTime(),
        targetPaper = targetPaper.toDomain(),
        sourcePaper = sourcePaper.toDomain(),
        targetValue = targetValue.toDecimal(),
        totalPriceInSource = totalPriceInSource.toDecimal(),
        pricePerUnit = pricePerUnit.toDecimal(),
        settlementRate = settlementRate.toDecimal(),
        fee = fee.toDecimal(),
        feePaper = feePaper.toDomain(),
        type = TransactionType.valueOf(type)
    )

    companion object : IntEntityClass<TransactionEntity>(TransactionTable)
}
