package com.marzec

import com.marzec.model.dto.ExerciseDto
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import react.child
import react.dom.render
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.RProps
import react.dom.h1
import react.dom.li
import react.dom.ul
import react.functionalComponent
import react.useEffect
import react.useState

val endpoint = window.location.origin

suspend fun getExercises(): List<ExerciseDto> {
    return jsonClient.get(endpoint + ApiPath.EXERCISES)
}

fun main() {
    render(document.getElementById("root")) {
        child(App)
    }
}

private val scope = MainScope()

val App = functionalComponent<RProps> { _ ->
    val (exercises, setExercises) = useState(emptyList<ExerciseDto>())

    useEffect(dependencies = listOf()) {
        scope.launch {
            setExercises(getExercises())
        }
    }

    h1 {
        +"Exercises"
    }
    ul {
        exercises.forEach { item ->
            li {
                key = item.toString()
                +"[${item.id}] ${item.name} "
            }
        }
    }
}