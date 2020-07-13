package com.marzec.api

import com.marzec.model.dto.CategoryDto
import com.marzec.model.dto.EquipmentDto
import com.marzec.model.dto.ExerciseDto
import com.marzec.model.http.HttpResponse

interface Controller {

    fun getExercises(): HttpResponse<List<ExerciseDto>>
    fun getCategories(): HttpResponse<List<CategoryDto>>
    fun getEquipment(): HttpResponse<List<EquipmentDto>>
}