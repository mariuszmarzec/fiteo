package com.marzec.views

import com.marzec.extensions.emptyString
import com.marzec.views.base.ReactRendererDelegate
import com.marzec.views.base.Renderer
import com.marzec.views.base.RendererDelegate
import com.marzec.views.base.ViewItem
import react.ChildrenBuilder
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.h5

enum class HeaderSize {
    BIG,
    MEDIUM,
    SMALL
}

sealed class HeaderViewItem(
    override val id: String = emptyString(),
    open val message: String,
    open val size: HeaderSize
) : ViewItem {
    override fun check(render: RendererDelegate): Boolean {
        return render is HeaderDelegate
    }
}

data class BigHeaderViewItem(
    override val id: String = emptyString(),
    override val message: String
) : HeaderViewItem(id, message, HeaderSize.BIG)

data class MediumHeaderViewItem(
    override val id: String = emptyString(),
    override val message: String
) : HeaderViewItem(id, message, HeaderSize.MEDIUM)

class HeaderDelegate : ReactRendererDelegate() {

    override fun ChildrenBuilder.render(renderer: Renderer, item: ViewItem) {
        item as HeaderViewItem
        when (item.size) {
            HeaderSize.BIG -> h1 { +item.message }
            HeaderSize.MEDIUM -> h3 { +item.message }
            HeaderSize.SMALL -> h5 { +item.message }
        }
    }
}
