package com.marzec

import com.marzec.screen.exerciselist.view.ExerciseList
import kotlinx.browser.document
import kotlinx.coroutines.ExperimentalCoroutinesApi
import react.child
import react.dom.render


@ExperimentalCoroutinesApi
fun main() {
    render(document.getElementById("root")) {
        child(ExerciseList)
    }
}