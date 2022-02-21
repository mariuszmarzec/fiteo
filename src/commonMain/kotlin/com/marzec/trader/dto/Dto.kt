package com.marzec.trader.dto

import kotlinx.serialization.Serializable

@Serializable
data class PaperDto(
    val id: Long,
    val code: String,
    val name: String,
    val type: String
)

@Serializable
data class TransactionDto(
    val id: Long,
    val title: String,
    val date: String,
    val targetPaper: PaperDto,
    val sourcePaper: PaperDto,
    val targetValue: String,
    val totalPriceInSource: String,
    val pricePerUnit: String,
    val settlementRate: String,
    val fee: String,
    val feePaper: PaperDto,
    val type: String
)
