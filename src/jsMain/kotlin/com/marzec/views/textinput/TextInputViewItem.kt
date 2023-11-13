package com.marzec.views.textinput

import com.marzec.views.base.ReactRendererDelegate
import com.marzec.views.base.Renderer
import com.marzec.views.base.RendererDelegate
import com.marzec.views.base.ViewItem
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import react.ChildrenBuilder
import react.dom.div
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.input

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

    override fun ChildrenBuilder.render(renderer: Renderer, item: ViewItem) {
        item as TextInputViewItem
        div {
            input {
                type = web.html.InputType.text
                name = item.text
                key = item.id

                value = item.text
                placeholder = item.hint
                onChange = {
                    val target = it.target
                    onTextChange(item.id, target.value)
                }
            }
        }
    }
}
