package com.marzec.api

import com.marzec.fiteo.ApiPath
import com.marzec.jsonClient
import com.marzec.model.domain.Category
import com.marzec.model.domain.Equipment
import com.marzec.model.domain.Exercise
import com.marzec.model.dto.CategoryDto
import com.marzec.model.dto.EquipmentDto
import com.marzec.model.dto.ExerciseDto
import com.marzec.model.dto.toDomain
import io.ktor.client.request.get
import kotlinx.browser.window

val endpoint = window.location.origin

suspend fun getExercises(): List<Exercise> =
    jsonClient.get<List<ExerciseDto>>(endpoint + ApiPath.EXERCISES)
        .map { it.toDomain() }

suspend fun getCategories(): List<Category> =
    jsonClient.get<List<CategoryDto>>(endpoint + ApiPath.CATEGORIES)
        .map { it.toDomain() }

suspend fun getEquipment(): List<Equipment> =
    jsonClient.get<List<EquipmentDto>>(endpoint + ApiPath.EQUIPMENT)
        .map { it.toDomain() }