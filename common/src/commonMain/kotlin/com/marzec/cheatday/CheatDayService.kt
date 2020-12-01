package com.marzec.cheatday

import com.marzec.cheatday.domain.Weight

class CheatDayService(
        private val weightsRepository: WeightsRepository
) {
    fun getWeights(userId: Int): List<Weight> {
        return weightsRepository.getWeights(userId)
    }
}
