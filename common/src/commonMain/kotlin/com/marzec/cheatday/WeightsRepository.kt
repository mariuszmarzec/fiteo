package com.marzec.cheatday

import com.marzec.cheatday.domain.Weight

interface WeightsRepository {

    fun getWeights(userId: Int): List<Weight>
}