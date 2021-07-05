package com.marzec

import com.marzec.screen.exerciselist.view.ExerciseList
import kotlinx.browser.document
import kotlinx.coroutines.ExperimentalCoroutinesApi
import react.child
import react.dom.render
import react.router.dom.hashRouter
import react.router.dom.switch
import react.router.dom.route

@ExperimentalCoroutinesApi
fun main() {
    render(document.getElementById("root")) {
        hashRouter {
            switch {
                route("/") {
                    child(ExerciseList)
                }
            }
        }
    }
}