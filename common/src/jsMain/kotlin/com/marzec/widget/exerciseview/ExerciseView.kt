package com.marzec.widget.exerciseview

import com.marzec.views.exerciserowview.ExerciseRowViewItem
import react.RProps
import react.dom.div
import react.dom.h3
import react.dom.img
import react.functionalComponent
import react.useState

external interface ExerciseRowViewProps : RProps {
    var exercise: ExerciseRowViewItem
}

val ExerciseRowView = functionalComponent<ExerciseRowViewProps> { props ->
    val (exercise, _) = useState(props.exercise)

    val imageUrl = exercise.animationUrl ?: exercise.imageUrl

    div {
        h3 { +exercise.name }
        imageUrl?.let { animationUrl -> img { attrs.src = animationUrl } }
    }
}