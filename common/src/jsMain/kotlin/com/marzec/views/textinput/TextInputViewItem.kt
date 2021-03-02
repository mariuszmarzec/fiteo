package com.marzec.views.textinput

import com.marzec.views.base.ReactRendererDelegate
import com.marzec.views.base.RendererDelegate
import com.marzec.views.base.ViewItem
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.dom.div
import react.dom.input
import react.key

data class TextInputViewItem(
        override val id: String,
        val text: String,
        val hint: String
) : ViewItem {
    override fun check(render: RendererDelegate): Boolean = render is TextInputDelegate
}

class TextInputDelegate(
        private val onTextChange: (id: String, text: String) -> Unit
) : ReactRendererDelegate() {

    override fun RBuilder.render(item: ViewItem) {
        item as TextInputViewItem
        div {
            input(type = InputType.text, name = item.text) {
                key = item.id

                attrs {
                    value = item.text
                    placeholder = item.hint
                    onChangeFunction = {
                        val target = it.target as HTMLInputElement
                        onTextChange(item.id, target.value)
                    }
                }
            }
        }
    }
}
