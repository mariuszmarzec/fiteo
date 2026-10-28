package com.marzec

import com.marzec.core.model.domain.toDto
import com.marzec.fiteo.ApiPath
import io.ktor.http.*
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext

class FeatureToggleTest {

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }

    @Test
    fun featureToggles() {
        testGetEndpoint(
            ApiPath.FEATURE_TOGGLES,
            HttpStatusCode.OK,
            featureToggles.map { it.toDto() },
            runRequestsBefore = {
                featureToggles.forEach {
                    runAddEndpoint(ApiPath.FEATURE_TOGGLES, it.toCreate())
                }
            }
        )
    }

    @Test
    fun getFeatureToggle() {
        testGetEndpoint(
            ApiPath.FEATURE_TOGGLE_BY_ID.replace("{${Api.Args.ARG_ID}}", featureToggleOne.id.toString()),
            HttpStatusCode.OK,
            featureToggleOne.toDto(),
            runRequestsBefore = {
                featureToggles.forEach {
                    runAddEndpoint(ApiPath.FEATURE_TOGGLES, it.toCreate())
                }
            }
        )
    }

    @Test
    fun createFeatureToggle() {
        testPostEndpoint(
            ApiPath.FEATURE_TOGGLES,
            newFeatureToggle,
            HttpStatusCode.OK,
            newFeatureToggle
        )
    }

    @Test
    fun updateFeatureToggle() {
        testPatchEndpoint(
            uri = ApiPath.FEATURE_TOGGLE_BY_ID.replace("{${Api.Args.ARG_ID}}", featureToggleOne.id.toString()),
            dto = mapOf("name" to "updated", "value" to "0"),
            status = HttpStatusCode.OK,
            responseDto = featureToggleOne.toDto().copy(name = "updated", value = "0"),
            runRequestsBefore = {
                runAddEndpoint(ApiPath.FEATURE_TOGGLES, newFeatureToggle)
            }
        )
    }


    @Test
    fun deleteFeatureToggle() {
        testDeleteEndpoint(
            uri = ApiPath.FEATURE_TOGGLE_BY_ID.replace("{${Api.Args.ARG_ID}}", featureToggleOne.id.toString()),
            status = HttpStatusCode.OK,
            responseDto = featureToggleOne.toDto(),
            runRequestsBefore = {
                runAddEndpoint(ApiPath.FEATURE_TOGGLES, newFeatureToggle)
            }
        )
    }
}