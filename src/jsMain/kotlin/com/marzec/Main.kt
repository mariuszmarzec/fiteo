package com.marzec

import com.marzec.screen.exerciselist.view.ExerciseList
import js.objects.jso
import kotlinx.browser.document
import kotlinx.coroutines.ExperimentalCoroutinesApi
import react.create
import react.dom.client.createRoot
import react.router.dom.RouterProvider
import react.router.dom.createBrowserRouter
import web.html.HTMLElement

@ExperimentalCoroutinesApi
fun main() {

    val browserRouter = createBrowserRouter(
        arrayOf(
            jso {
                path = "/"
                Component = ExerciseList
            }
        )
    )
    val root = createRoot(document.getElementById("root").unsafeCast<HTMLElement>())
    root.render(
        RouterProvider.create {
            router = browserRouter
        }
    )
}
