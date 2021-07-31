package com.marzec.cheatday

import com.marzec.cheatday.domain.Weight
import kotlinx.datetime.LocalDateTime

class CheatDayService(
        private val weightsRepository: WeightsRepository
) {
    fun getWeights(userId: Int): List<Weight> {
        return weightsRepository.getWeights(userId)
    }

    fun putWeight(userId: Int, weight: Float, date: String): Weight {
        return weightsRepository.addWeight(userId, weight, LocalDateTime.parse(date))
    }

    fun removeWeight(userId: Int, weightId: Int): Weight {
        return weightsRepository.removeWeight(userId, weightId)
    }

    fun updateWeight(userId: Int, weight: Weight): Weight {
        return weightsRepository.updateWeight(userId, weight)
    }
}
