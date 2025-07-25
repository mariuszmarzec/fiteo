package com.marzec.views.horizontalsplitview

import com.marzec.views.base.ReactRenderer
import com.marzec.views.base.ReactRendererDelegate
import com.marzec.views.base.Renderer
import com.marzec.views.base.RendererDelegate
import com.marzec.views.base.ViewItem
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import web.cssom.Display
import web.cssom.pct
import emotion.react.css

data class HorizontalSplitView(
    override val id: String,
    val leftColumnItems: List<ViewItem>,
    val rightColumnItems: List<ViewItem>,
    val leftPercentageWidth: Int = 50,
    val rightPercentageWidth: Int = 50
) : ViewItem {

    override fun check(render: RendererDelegate): Boolean {
        return render is HorizontalSplitDelegate
    }
}

class HorizontalSplitDelegate : ReactRendererDelegate() {

    override fun ChildrenBuilder.render(renderer: Renderer, item: ViewItem) {
        item as HorizontalSplitView
        renderer as ReactRenderer
        div {
            css {
                height = 100.pct
                width = 100.pct
                display = Display.flex
            }
            div {
                css {
                    width = item.leftPercentageWidth.pct
                }
                renderer.render(item.leftColumnItems, this)
            }

            div {
                css {
                    width = item.rightPercentageWidth.pct
                }
                renderer.render(item.rightColumnItems, this)
            }
        }

    }
}
