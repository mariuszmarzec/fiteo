package com.marzec.fiteo.repositories

import com.marzec.core.model.domain.FeatureToggle
import com.marzec.core.model.domain.NewFeatureToggle
import com.marzec.core.model.domain.UpdateFeatureToggle
import com.marzec.core.repository.CommonRepository

interface FeatureTogglesRepository : CommonRepository<Int, FeatureToggle, NewFeatureToggle, UpdateFeatureToggle>