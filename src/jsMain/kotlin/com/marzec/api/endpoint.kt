package com.marzec.api

import com.marzec.fiteo.ApiPath
import com.marzec.jsonClient
import com.marzec.fiteo.model.domain.Category
import com.marzec.fiteo.model.domain.Equipment
import com.marzec.fiteo.model.domain.Exercise
import com.marzec.fiteo.model.dto.CategoryDto
import com.marzec.fiteo.model.dto.EquipmentDto
import com.marzec.fiteo.model.dto.ExerciseDto
import com.marzec.fiteo.model.dto.toDomain
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.browser.window

val endpoint = window.location.origin

suspend fun getExercises(): List<Exercise> =
    jsonClient.get(endpoint + ApiPath.EXERCISES).body<List<ExerciseDto>>()
        .map { it.toDomain() }

suspend fun getCategories(): List<Category> =
    jsonClient.get(endpoint + ApiPath.CATEGORIES).body<List<CategoryDto>>()
        .map { it.toDomain() }

suspend fun getEquipment(): List<Equipment> =
    jsonClient.get(endpoint + ApiPath.EQUIPMENT).body<List<EquipmentDto>>()
        .map { it.toDomain() }
