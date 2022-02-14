package com.marzec.trader.model

import com.marzec.trader.dto.TransactionDto

data class Transaction(
    val id: Long,
)

fun Transaction.toDto(): TransactionDto = TransactionDto(
        id = id
)

fun TransactionDto.toDomain(): Transaction = Transaction(
            id = id,
    )
