package com.marzec.cheatday

import com.marzec.cheatday.domain.CreateWeight
import com.marzec.cheatday.domain.UpdateWeight
import com.marzec.cheatday.domain.Weight
import kotlinx.datetime.LocalDateTime

class CheatDayService(
        private val weightsRepository: WeightsRepository
) {
    fun getWeights(userId: Int): List<Weight> {
        return weightsRepository.getAll(userId)
    }

    fun getWeight(userId: Int, weightId: Int): Weight =
        weightsRepository.getById(userId, weightId)

    fun putWeight(userId: Int, weight: Float, date: String): Weight {
        return weightsRepository.create(userId, CreateWeight(weight, LocalDateTime.parse(date)))
    }

    fun removeWeight(userId: Int, weightId: Int): Weight {
        return weightsRepository.delete(userId, weightId)
    }

    fun updateWeight(userId: Int, weightId: Int, weight: UpdateWeight): Weight =
        weightsRepository.update(userId, weightId, weight)
}
