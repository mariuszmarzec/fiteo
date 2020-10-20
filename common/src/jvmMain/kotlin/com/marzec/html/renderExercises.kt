package com.marzec.html

import com.marzec.model.dto.CategoryDto
import com.marzec.model.dto.ExerciseDto
import com.marzec.model.http.HttpResponse
import kotlinx.html.BODY
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h3
import kotlinx.html.img
import kotlinx.html.span

fun HTML.renderExercises(response: HttpResponse<List<ExerciseDto>>) {
    when (response) {
        is HttpResponse.Success -> {
            val exercises = response.data.groupBy { it.category }
            body {
                exercises.forEach {
                    renderExercise(it.key, it.value)
                }
            }
        }
        is HttpResponse.Error -> {
            body {
                span {
                    +"Error during lading page"
                }
            }
        }
    }
}

fun BODY.renderExercise(categoryDto: CategoryDto, exercises: List<ExerciseDto>) {
    h1 { +categoryDto.name }
    div {
        exercises.forEach {  exercise ->
            div {
                h3 { +exercise.name }
                img { src = exercise.animationUrl }
            }
        }
    }
}
