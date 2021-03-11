package com.marzec.views.horizontalsplitview

import com.marzec.views.base.*
import kotlinx.css.Display
import kotlinx.css.LinearDimension
import kotlinx.css.display
import react.RBuilder
import styled.StyleSheet
import styled.*
import kotlinx.css.*

data class HorizontalSplitView(
    override val id: String,
    val leftColumnItems: List<ViewItem>,
    val rightColumnItems: List<ViewItem>,
    val leftPercentageWidth: String = "50%",
    val rightPercentageWidth: String = "50%"
) : ViewItem {

    override fun check(render: RendererDelegate): Boolean {
        return render is HorizontalSplitDelegate
    }
}

class HorizontalSplitDelegate : ReactRendererDelegate() {

    override fun RBuilder.render(renderer: Renderer, item: ViewItem) {
        item as HorizontalSplitView
        renderer as ReactRenderer
        styledDiv {
            css {
                height = LinearDimension("100%")
                width = LinearDimension("100%")
                display = Display.flex
            }
            styledDiv {
                css {
                    width = LinearDimension(item.leftPercentageWidth)
                }
                renderer.render(item.leftColumnItems, this)
            }

            styledDiv {
                css {
                    width = LinearDimension(item.rightPercentageWidth)
                }
                renderer.render(item.rightColumnItems, this)
            }
        }

    }
}