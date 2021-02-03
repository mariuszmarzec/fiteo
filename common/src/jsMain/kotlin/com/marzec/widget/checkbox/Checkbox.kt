package com.marzec.widget.checkbox

import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import react.RProps
import react.dom.div
import react.dom.input
import react.dom.label
import react.functionalComponent
import react.useState

external interface CheckboxProps : RProps {
    var state: CheckboxModel
    var onCheckedChange: () -> Unit
}

data class CheckboxModel(
    val viewId: String,
    val label: String,
    val isChecked: Boolean
)

val Checkbox = functionalComponent<CheckboxProps> { props ->
    val (state, _) = useState(props.state)

    div {
        input(type = InputType.checkBox) {
            attrs {
                checked = state.isChecked
                id = state.viewId
                value = state.label
                onChangeFunction = {
                    props.onCheckedChange()
                }
            }
        }
        label {
            +state.label
            attrs["htmlFor"] = state.viewId
        }
    }
}