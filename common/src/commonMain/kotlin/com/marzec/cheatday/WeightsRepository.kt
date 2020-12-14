package com.marzec.cheatday

import com.marzec.cheatday.domain.Weight
import kotlinx.datetime.LocalDateTime

interface WeightsRepository {

    fun getWeights(userId: Int): List<Weight>

    fun addWeight(userId: Int, weight: Float, date: LocalDateTime): Weight
}