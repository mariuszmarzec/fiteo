package com.marzec

import com.marzec.fiteo.ApiPath
import com.marzec.fiteo.model.domain.toDto
import com.marzec.fiteo.model.dto.EquipmentDto
import com.marzec.fiteo.model.dto.ErrorDto
import io.ktor.http.*
import kotlinx.serialization.json.JsonPrimitive
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext

class FiteoCoreTest {

    val newEquipment = EquipmentDto("equipment_id", "test_name")
    val newCategory = EquipmentDto("category_id", "test_name")

    @Test
    fun exercises() {
        testGetEndpoint(
            ApiPath.EXERCISES,
            HttpStatusCode.OK,
            exercises.map { it.toDto() }
        )
    }

    @Test
    fun equipment() {
        testGetEndpoint(
            ApiPath.EQUIPMENT,
            HttpStatusCode.OK,
            equipment.map { it.toDto() }
        )
    }

    @Test
    fun createEquipment() {
        testPostEndpoint(
            ApiPath.EQUIPMENT,
            newEquipment,
            HttpStatusCode.OK,
            newEquipment
        )
    }
    
    @Test
    fun updateEquipment() {
        testPatchEndpoint(
            uri = ApiPath.EQUIPMENT_BY_ID.replace("{${Api.Args.ARG_ID}}", newEquipment.id),
            dto = mapOf("name" to JsonPrimitive("updated")),
            status = HttpStatusCode.OK,
            responseDto = newEquipment.copy(name = "updated"),
            runRequestsBefore = {
                runAddEndpoint(ApiPath.EQUIPMENT, newEquipment)
            }
        )
    }


    @Test
    fun deleteEquipment() {
        testDeleteEndpoint(
            uri = ApiPath.EQUIPMENT_BY_ID.replace("{${Api.Args.ARG_ID}}", newEquipment.id),
            status = HttpStatusCode.OK,
            responseDto = newEquipment,
            runRequestsBefore = {
                runAddEndpoint(ApiPath.EQUIPMENT, newEquipment)
            }
        )
    }

    @Test
    fun categories() {
        testGetEndpoint(
            ApiPath.CATEGORIES,
            HttpStatusCode.OK,
            categories.map { it.toDto() }
        )
    }

    @Test
    fun createCategory() {
        testPostEndpoint(
            ApiPath.CATEGORIES,
            newCategory,
            HttpStatusCode.OK,
            newCategory
        )
    }

    @Test
    fun updateCategory() {
        testPatchEndpoint(
            uri = ApiPath.CATEGORY_BY_ID.replace("{${Api.Args.ARG_ID}}", "category_id"),
            dto = mapOf("name" to "updated"),
            status = HttpStatusCode.OK,
            responseDto = newCategory.copy(name = "updated"),
            runRequestsBefore = {
                runAddEndpoint(ApiPath.CATEGORIES, newCategory)
            }
        )
    }


    @Test
    fun deleteCategory() {
        testDeleteEndpoint(
            uri = ApiPath.CATEGORY_BY_ID.replace("{${Api.Args.ARG_ID}}", "category_id"),
            status = HttpStatusCode.OK,
            responseDto = newCategory,
            runRequestsBefore = {
                runAddEndpoint(ApiPath.CATEGORIES, newCategory)
            }
        )
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }
}
