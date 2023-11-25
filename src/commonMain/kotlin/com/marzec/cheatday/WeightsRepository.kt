package com.marzec.cheatday

import com.marzec.cheatday.domain.Weight
import kotlinx.datetime.LocalDateTime

interface WeightsRepository {

    fun getWeights(userId: Int): List<Weight>

    fun getWeight(userId: Int, weightId: Int): Weight

    fun addWeight(userId: Int, weight: Float, date: LocalDateTime): Weight

    fun removeWeight(userId: Int, weightId: Int): Weight

    fun updateWeight(userId: Int, weight: Weight): Weight
}
