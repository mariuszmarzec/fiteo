package com.marzec.fiteo.repositories

import com.marzec.core.model.domain.FeatureToggle
import com.marzec.core.model.domain.NewFeatureToggle
import com.marzec.core.model.domain.UpdateFeatureToggle
import com.marzec.core.repository.CommonRepository
import org.jetbrains.exposed.v1.jdbc.Database

class FeatureTogglesRepositoryImpl(
    private val database: Database,
    private val repository: CommonRepository<Int, FeatureToggle, NewFeatureToggle, UpdateFeatureToggle>
) : FeatureTogglesRepository, CommonRepository<Int, FeatureToggle, NewFeatureToggle, UpdateFeatureToggle> by repository