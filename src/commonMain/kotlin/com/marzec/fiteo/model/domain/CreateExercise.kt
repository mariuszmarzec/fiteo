package com.marzec.fiteo.model.domain

data class CreateExercise(
        val name: String,
        val animationUrl: String?,
        val videoUrl: String?,
        val category: List<Category>,
        val neededEquipment: List<Equipment>,
        val thumbnailUrl: String?
)
