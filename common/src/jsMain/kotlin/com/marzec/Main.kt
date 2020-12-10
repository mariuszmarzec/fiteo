package com.marzec

import com.marzec.model.domain.Exercise
import com.marzec.model.dto.ExerciseDto
import com.marzec.model.dto.toDomain
import io.ktor.client.request.get
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.html.h1
import react.RProps
import react.child
import react.dom.div
import react.dom.h1
import react.dom.h3
import react.dom.img
import react.dom.li
import react.dom.render
import react.dom.ul
import react.functionalComponent
import react.useEffect
import react.useState

val endpoint = window.location.origin

suspend fun getExercises(): List<Exercise> =
        jsonClient.get<List<ExerciseDto>>(endpoint + ApiPath.EXERCISES)
                .map { it.toDomain() }

fun main() {
    render(document.getElementById("root")) {
        child(App)
    }
}

private val scope = MainScope()

val App = functionalComponent<RProps> { _ ->
    val (exercises, setExercises) = useState(emptyList<Exercise>())

    useEffect(dependencies = listOf()) {
        scope.launch {
            setExercises(getExercises())
        }
    }

    ul {
        exercises.groupBy { it.category }.forEach { (categories, exercises) ->
            h1 { +categories.fold("") { acc, value -> "$acc${value.name} " } }
            exercises.forEach { exercise ->
                child(ExercisesView) {
                    this.attrs.exercise = exercise
                }
            }
        }
    }
}

val ExercisesView = functionalComponent<ExercisesViewProps> { props ->
    val (exercise, _) = useState(props.exercise)

    div {
        h3 { +exercise.name }
        exercise.animationUrl?.let { animationUrl -> img { attrs.src = animationUrl } }
    }
}

external interface ExercisesViewProps : RProps {
    var exercise: Exercise
}