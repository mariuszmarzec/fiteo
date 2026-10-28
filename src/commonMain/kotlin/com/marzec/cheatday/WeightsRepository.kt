package com.marzec.cheatday

import com.marzec.cheatday.domain.CreateWeight
import com.marzec.cheatday.domain.UpdateWeight
import com.marzec.cheatday.domain.Weight
import com.marzec.core.repository.CommonWithUserRepository

interface WeightsRepository : CommonWithUserRepository<Weight, CreateWeight, UpdateWeight>