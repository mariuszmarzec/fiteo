package com.marzec.fiteo.services

import com.marzec.core.model.domain.FeatureToggle
import com.marzec.core.model.domain.NewFeatureToggle
import com.marzec.core.model.domain.UpdateFeatureToggle
import com.marzec.fiteo.model.domain.*
import com.marzec.fiteo.repositories.CategoriesRepository
import com.marzec.fiteo.repositories.EquipmentRepository
import com.marzec.fiteo.repositories.ExercisesRepository
import com.marzec.fiteo.repositories.FeatureTogglesRepository

interface FeatureTogglesService {

    fun getFeatureToggles(): List<FeatureToggle>
    fun getFeatureToggle(id: Int): FeatureToggle
    fun createFeatureToggle(featureToggle: NewFeatureToggle): FeatureToggle
    fun updateFeatureToggle(id: Int, update: UpdateFeatureToggle): FeatureToggle
    fun deleteFeatureToggle(id: Int): FeatureToggle
}

class FeatureTogglesServiceImpl(
        private val featureTogglesRepository: FeatureTogglesRepository
) : FeatureTogglesService {

    override fun getFeatureToggles(): List<FeatureToggle> = featureTogglesRepository.getAll()

    override fun getFeatureToggle(id: Int): FeatureToggle = featureTogglesRepository.getById(id)

    override fun createFeatureToggle(featureToggle: NewFeatureToggle): FeatureToggle = featureTogglesRepository.create(featureToggle)

    override fun updateFeatureToggle(id: Int, update: UpdateFeatureToggle): FeatureToggle = featureTogglesRepository.update(id, update)

    override fun deleteFeatureToggle(id: Int): FeatureToggle = featureTogglesRepository.delete(id)
}
