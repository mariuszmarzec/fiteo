package com.marzec.views.horizontalsplitview

import com.marzec.views.base.ReactRenderer
import com.marzec.views.base.ReactRendererDelegate
import com.marzec.views.base.Renderer
import com.marzec.views.base.RendererDelegate
import com.marzec.views.base.ViewItem
import emotion.react.css
import react.ChildrenBuilder
import react.create
import react.dom.html.ReactHTML.div
import web.cssom.Display
import web.cssom.pct

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
        div.create {
            css {
                height = 100.pct
                width = 100.pct
                display = Display.flex
            }
            div.create {
                css {
                    width = item.leftPercentageWidth.pct
                }
                renderer.render(item.leftColumnItems, this)
            }

            div.create {
                css {
                    width = item.rightPercentageWidth.pct
                }
                renderer.render(item.rightColumnItems, this)
            }
        }

    }
}
