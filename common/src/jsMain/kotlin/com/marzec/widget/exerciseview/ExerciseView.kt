package com.marzec.widget.exerciseview

import com.marzec.model.domain.Category
import com.marzec.model.domain.Exercise
import react.RProps
import react.dom.div
import react.dom.h3
import react.dom.img
import react.functionalComponent
import react.useState

data class ExerciseViewModel(
    val id: Int,
    val name: String,
    val animationUrl: String?,
    val imageUrl: String?,
    val category: List<Category>
)

external interface ExerciseViewProps : RProps {
    var exercise: ExerciseViewModel
}

val ExerciseView = functionalComponent<ExerciseViewProps> { props ->
    val (exercise, _) = useState(props.exercise)

    val imageUrl = exercise.animationUrl ?: exercise.imageUrl

    div {
        h3 { +exercise.name }
        imageUrl?.let { animationUrl -> img { attrs.src = animationUrl } }
    }
}

fun Exercise.toView() = ExerciseViewModel(
    id = id,
    name = name,
    animationUrl = animationUrl,
    imageUrl = imagesUrls?.firstOrNull(),
    category = category
)