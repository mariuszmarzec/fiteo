package com.marzec.fiteo.model.domain

import com.marzec.Api
import com.marzec.exceptions.HttpException
import com.marzec.exceptions.HttpStatus
import com.marzec.extensions.formatDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

data class Training(
    val id: Int,
    val templateId: Int,
    val createDateInMillis: LocalDateTime,
    val finishDateInMillis: LocalDateTime,
    val exercisesWithProgress: List<TrainingExerciseWithProgress>
)

@Serializable
data class TrainingDto(
    val id: Int,
    val templateId: Int,
    val createDateInMillis: String,
    val finishDateInMillis: String,
    val exercisesWithProgress: List<TrainingExerciseWithProgressDto>
)

fun Training.toDto() = TrainingDto(
    id = id,
    templateId = templateId,
    createDateInMillis = createDateInMillis.formatDate(),
    finishDateInMillis = finishDateInMillis.formatDate(),
    exercisesWithProgress = exercisesWithProgress.map { it.toDto() }
)

data class UpdateTraining(
    val finishDateInMillis: LocalDateTime,
    val exercisesWithProgress: List<UpdateTrainingExerciseWithProgress>
)

@Serializable
data class UpdateTrainingDto(
    val finishDateInMillis: String,
    val exercisesWithProgress: List<UpdateTrainingExerciseWithProgressDto>
)

@Serializable
data class CreateTrainingDto(
    val templateTrainingId: Int
)

data class UpdateTrainingExerciseWithProgress(
    val id: Int?,
    val exerciseId: Int,
    val series: List<Series>,
    val trainingPartId: Int?,
    val name: String
)

@Serializable
data class UpdateTrainingExerciseWithProgressDto(
    val id: Int? = null,
    val exerciseId: Int,
    val series: List<SeriesDto>,
    val trainingPartId: Int?,
    val name: String
)

fun UpdateTrainingDto.toDomain() = UpdateTraining(
    finishDateInMillis = LocalDateTime.parse(finishDateInMillis),
    exercisesWithProgress = exercisesWithProgress.map { it.toDomain() },
)

fun UpdateTrainingExerciseWithProgressDto.toDomain() = UpdateTrainingExerciseWithProgress(
    id = id,
    exerciseId = exerciseId,
    series = series.map { it.toDomain() },
    trainingPartId = trainingPartId,
    name = name
)

/**
 * Pagination request parsed from the `limit` and `cursor` query parameters.
 *
 * When neither parameter is present the request is a "legacy" one and the
 * endpoint must return the full list of trainings as a plain JSON array.
 */
data class TrainingsRequest(
    val limit: Int? = null,
    val cursor: Int? = null
) {
    companion object {
        private const val DEFAULT_LIMIT = 10
        private const val MIN_LIMIT = 1
        private const val MAX_LIMIT = 100

        fun from(queries: Map<String, List<String>>): TrainingsRequest {
            val limitRaw = queries[Api.Args.ARG_LIMIT]?.firstOrNull()
            val cursorRaw = queries[Api.Args.ARG_CURSOR]?.firstOrNull()

            // No pagination parameters -> legacy response.
            if (limitRaw.isNullOrBlank() && cursorRaw.isNullOrBlank()) {
                return TrainingsRequest()
            }

            val limit = limitRaw?.let { value ->
                value.toIntOrNull() ?: throw HttpException(
                    "Argument ${Api.Args.ARG_LIMIT} must be an integer between $MIN_LIMIT and $MAX_LIMIT",
                    HttpStatus.BAD_REQUEST
                )
            } ?: DEFAULT_LIMIT
            if (limit < MIN_LIMIT || limit > MAX_LIMIT) {
                throw HttpException(
                    "Argument ${Api.Args.ARG_LIMIT} must be between $MIN_LIMIT and $MAX_LIMIT",
                    HttpStatus.BAD_REQUEST
                )
            }

            val cursor = cursorRaw?.let { value ->
                value.toIntOrNull() ?: throw HttpException(
                    "Argument ${Api.Args.ARG_CURSOR} must be a positive integer",
                    HttpStatus.BAD_REQUEST
                )
            }
            if (cursor != null && cursor <= 0) {
                throw HttpException(
                    "Argument ${Api.Args.ARG_CURSOR} must be a positive integer",
                    HttpStatus.BAD_REQUEST
                )
            }

            return TrainingsRequest(limit = limit, cursor = cursor)
        }
    }
}

/** Domain representation of a paginated response. */
data class TrainingsPage(
    val items: List<Training>,
    val nextCursor: Int?
)

/** Serializable representation of a paginated response. */
@Serializable
data class TrainingsPageDto(
    val items: List<TrainingDto>,
    val nextCursor: Int?
)

fun TrainingsPage.toDto() = TrainingsPageDto(
    items = items.map { it.toDto() },
    nextCursor = nextCursor
)
