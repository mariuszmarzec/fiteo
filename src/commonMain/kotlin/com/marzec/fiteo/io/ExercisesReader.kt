package com.marzec.fiteo.io

import com.marzec.fiteo.model.dto.ExercisesFileDto
import kotlinx.serialization.json.Json

interface ExercisesReader {

    fun parse(json: String): ExercisesFileDto
}

class ExercisesReaderImpl(
        private val json: Json
) : ExercisesReader {

    override fun parse(json: String): ExercisesFileDto {
        return this.json.decodeFromString(ExercisesFileDto.serializer(), json)
    }
}
