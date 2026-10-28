package com.marzec.views.base
interface ViewItem {

    val id: String

    fun check(render: RendererDelegate): Boolean
}

