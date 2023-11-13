package com.marzec

import com.marzec.screen.exerciselist.view.ExerciseList
import kotlinx.browser.document
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.html.HTML
import react.child
import react.dom.client.RootOptions
import react.dom.client.createRoot
import react.dom.render
import web.html.HTMLElement

@ExperimentalCoroutinesApi
fun main() {
    val root = createRoot(document.getElementById("root").unsafeCast<HTMLElement>())
    root.render {
        hashRouter {
            switch {
                route("/") {
                    child(ExerciseList)
                }
            }
        }

    }
}
