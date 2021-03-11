package com.marzec.views.loading

import com.marzec.extensions.emptyString
import com.marzec.views.base.ReactRendererDelegate
import com.marzec.views.base.Renderer
import com.marzec.views.base.RendererDelegate
import com.marzec.views.base.ViewItem
import com.marzec.views.error.ErrorItemView
import react.RBuilder
import react.dom.h3

data class LoadingItemView(override val id: String = emptyString()) : ViewItem {

    override fun check(render: RendererDelegate): Boolean = render is LoadingDelegate
}

class LoadingDelegate : ReactRendererDelegate() {

    override fun RBuilder.render(renderer: Renderer, item: ViewItem) {        item as LoadingItemView
        h3 { +"Loading" }
    }
}