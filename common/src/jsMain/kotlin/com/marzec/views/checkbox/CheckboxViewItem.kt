package com.marzec.views.checkbox

import com.marzec.views.base.ReactRendererDelegate
import com.marzec.views.base.Renderer
import com.marzec.views.base.RendererDelegate
import com.marzec.views.base.ViewItem
import com.marzec.widget.checkbox.Checkbox
import react.RBuilder
import react.child
import react.key

data class CheckboxViewItem(
    override val id: String,
    val label: String,
    val isChecked: Boolean
) : ViewItem {
    override fun check(render: RendererDelegate): Boolean = render is CheckboxDelegate
}

class CheckboxDelegate(
    private val onCheckedChange: (String) -> Unit
) : ReactRendererDelegate() {

    override fun RBuilder.render(renderer: Renderer, item: ViewItem) {
        item as CheckboxViewItem
        child(Checkbox) {
            attrs {
                state = item
                key = item.id
                onCheckedChange = {
                    onCheckedChange(item.id)
                }
            }
        }
    }
}
