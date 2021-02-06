package com.marzec.widget.checkbox

import com.marzec.views.checkbox.CheckboxViewItem
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
    var state: CheckboxViewItem
    var onCheckedChange: () -> Unit
}

val Checkbox = functionalComponent<CheckboxProps> { props ->
    val (state, _) = useState(props.state)

    div {
        attrs {
            key = id
        }
        input(type = InputType.checkBox) {
            attrs {
                checked = state.isChecked
                id = state.id
                value = state.label
                onChangeFunction = {
                    props.onCheckedChange()
                }
            }
        }
        label {
            +state.label
            attrs["htmlFor"] = state.id
        }
    }
}