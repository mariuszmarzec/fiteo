package com.marzec.trader.model

import com.marzec.core.Decimal
import com.marzec.core.toDecimal
import com.marzec.extensions.formatDate
import com.marzec.trader.dto.PaperDto
import com.marzec.trader.dto.PaperTagDto
import com.marzec.trader.dto.TransactionDto
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime

data class Paper(
    val id: Long,
    val code: String,
    val name: String,
    val type: PaperType,
    val tags: List<PaperTag>?
)

data class PaperTag(
    val id: Long,
    val name: String
)

enum class PaperType {
    SETTLEMENT_CURRENCY,
    CURRENCY,
    CRYPTO_CURRENCY,
    SHARE,
    COMMODITY
}

data class Transaction(
    val id: Long,
    val title: String,
    val date: LocalDateTime,
    val targetPaper: Paper,
    val sourcePaper: Paper,
    val targetValue: Decimal,
    val totalPriceInSource: Decimal,
    val pricePerUnit: Decimal,
    val settlementRate: Decimal,
    val fee: Decimal,
    val feePaper: Paper,
    val type: TransactionType
)

enum class TransactionType {
    FEE,
    SALE,
    PURCHASE,
    COST,
    DIVIDEND
}

fun Paper.toDto(): PaperDto = PaperDto(
    id = id,
    code = code,
    name = name,
    type = type.toString(),
    tags = tags?.map { it.toDto() }
)

fun PaperDto.toDomain(): Paper = Paper(
    id = id,
    code = code,
    name = name,
    type = PaperType.valueOf(type),
    tags = tags?.map { it.toDomain() }
)

fun PaperTag.toDto(): PaperTagDto = PaperTagDto(
    id = id,
    name = name
)

fun PaperTagDto.toDomain(): PaperTag = PaperTag(
    id = id,
    name = name
)

fun Transaction.toDto(): TransactionDto = TransactionDto(
    id = id,
    title = title,
    date = date.formatDate(),
    targetPaper = targetPaper.toDto(),
    sourcePaper = sourcePaper.toDto(),
    targetValue = targetValue.toString(),
    totalPriceInSource = totalPriceInSource.toString(),
    pricePerUnit = pricePerUnit.toString(),
    settlementRate = settlementRate.toString(),
    fee = fee.toString(),
    feePaper = feePaper.toDto(),
    type = type.toString(),
)

fun TransactionDto.toDomain(): Transaction = Transaction(
    id = id,
    title = title,
    date = date.toLocalDateTime(),
    targetPaper = targetPaper.toDomain(),
    sourcePaper = sourcePaper.toDomain(),
    targetValue = targetValue.toDecimal(),
    totalPriceInSource = totalPriceInSource.toDecimal(),
    pricePerUnit = pricePerUnit.toDecimal(),
    settlementRate = settlementRate.toDecimal(),
    fee = fee.toDecimal(),
    feePaper = feePaper.toDomain(),
    type = TransactionType.valueOf(type),
)
