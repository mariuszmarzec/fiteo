package com.marzec.widget.exerciseview

import com.marzec.views.exerciserowview.ExerciseRowViewItem
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.img
import react.useState

val ExerciseRowView = FC<ExerciseRowViewProps> { props ->
    val exercise = props.exercise

    val imageUrl = exercise.animationUrl ?: exercise.imageUrl

    div {
        h3 { +exercise.name }
        imageUrl?.let { animationUrl -> img { src = animationUrl } }
    }
}

external interface ExerciseRowViewProps : Props {
    var exercise: ExerciseRowViewItem
}
