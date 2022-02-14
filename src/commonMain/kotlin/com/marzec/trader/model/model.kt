package com.marzec.trader.model

import com.marzec.trader.dto.TransactionDto
import kotlinx.datetime.LocalDateTime

data class Paper(
    val id: Long,
    val code: String,
    val name: String,
    val type: PaperType
)

data class PaperDto(
    val id: Long,
    val code: String,
    val name: String,
    val type: String
)

enum class PaperType {
    CURRENCY,
    SHARE,
    COMMODITY
}

data class Transaction(
    val id: Long,
    val title: String,
    val date: LocalDateTime,
    val targetPaper: Paper,
    val sourcePaper: Paper,
    val targetValue: String,
    val totalPriceInSource: String,
    val pricePerUnit: String,
    val rate: String,
    val fee: String,
    val feePaper: Paper,
    val type: TransactionType
)

enum class TransactionType {
    FEE,
    SALE,
    PURCHASE,
    COST
}

fun Paper.toDto(): PaperDto = PaperDto(
    id = id,
    code = code,
    name = name,
    type = type.toString(),
)

fun PaperDto.toDomain(): Paper = Paper(
    id = id,
    code = code,
    name = name,
    type = PaperType.valueOf(type),
)

//fun Transaction.toDto(): TransactionDto = TransactionDto(
//    id = id
//)
//
//fun TransactionDto.toDomain(): Transaction = Transaction(
//    id = id,
//)
