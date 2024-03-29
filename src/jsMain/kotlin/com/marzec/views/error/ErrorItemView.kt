package com.marzec.views.error

import com.marzec.extensions.emptyString
import com.marzec.views.base.ReactRendererDelegate
import com.marzec.views.base.Renderer
import com.marzec.views.base.RendererDelegate
import com.marzec.views.base.ViewItem
import react.ChildrenBuilder
import react.dom.html.ReactHTML.h3

data class ErrorItemView(
    override val id: String = emptyString(),
    val message: String
) : ViewItem {

    override fun check(render: RendererDelegate): Boolean = render is ErrorDelegate
}

class ErrorDelegate : ReactRendererDelegate() {

    override fun ChildrenBuilder.render(renderer: Renderer, item: ViewItem) {
        item as ErrorItemView
        h3 { +"Error: ${item.message}" }
    }
}
