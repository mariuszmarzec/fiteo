package com.marzec.cheatday

import com.marzec.cheatday.domain.UpdateWeight
import com.marzec.cheatday.domain.Weight
import kotlinx.datetime.LocalDateTime

class CheatDayService(
        private val weightsRepository: WeightsRepository
) {
    fun getWeights(userId: Int): List<Weight> {
        return weightsRepository.getWeights(userId)
    }

    fun getWeight(userId: Int, weightId: Int): Weight =
        weightsRepository.getWeight(userId, weightId)

    fun putWeight(userId: Int, weight: Float, date: String): Weight {
        return weightsRepository.addWeight(userId, weight, LocalDateTime.parse(date))
    }

    fun removeWeight(userId: Int, weightId: Int): Weight {
        return weightsRepository.removeWeight(userId, weightId)
    }

    fun updateWeight(userId: Int, weight: Weight): Weight {
        return weightsRepository.updateWeight(userId, weight)
    }

    fun updateWeight(userId: Int, weightId: Int, weight: UpdateWeight): Weight =
        weightsRepository.updateWeight(userId, weightId, weight)
}
