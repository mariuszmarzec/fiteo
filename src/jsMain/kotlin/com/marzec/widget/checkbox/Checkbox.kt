package com.marzec.widget.checkbox

import com.marzec.views.checkbox.CheckboxViewItem
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.useState

val Checkbox = FC<CheckboxProps> { props ->
//    val (state, _) = useState(props.state)
    val state = props.state

    console.log("state: ${state.label} ${state.isChecked}")

    div {
        input {
            type = web.html.InputType.checkbox
            checked = state.isChecked
            id = state.id
            value = state.label
            onChange = {
                props.onCheckedChange()
            }
        }
    }
    label {
        +state.label
        htmlFor = state.id
    }
}

external interface CheckboxProps : Props {
    var state: CheckboxViewItem
    var onCheckedChange: () -> Unit
}
