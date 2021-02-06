package com.marzec.views.base

import react.RBuilder

interface RendererDelegate {

    fun render(item: ViewItem)
}

abstract class ReactRendererDelegate : RendererDelegate {

    lateinit var builder: RBuilder

    override fun render(item: ViewItem): Unit = builder.render(item)

    abstract fun RBuilder.render(item: ViewItem)
}

interface Renderer {

    fun add(delegate: RendererDelegate): Renderer

    fun render(items: List<ViewItem>)
}

class RendererImpl : Renderer {

    private val delegates = mutableListOf<RendererDelegate>()

    override fun add(delegate: RendererDelegate): Renderer {
        delegates.add(delegate)
        return this
    }

    override fun render(items: List<ViewItem>) {
        items.forEach { item ->
            delegates.first { item.check(it) }.render(item)
        }
    }
}

class ReactRenderer : Renderer {

    lateinit var builder: RBuilder

    private val delegates = mutableListOf<RendererDelegate>()

    override fun add(delegate: RendererDelegate): Renderer {
        delegates.add(delegate)
        return this
    }

    override fun render(items: List<ViewItem>) {
        items.forEach { item ->
            val rendererDelegate = delegates.firstOrNull { item.check(it) }
                ?: throw NoSuchElementException("No delegates for item: ${item::class.simpleName}")
            (rendererDelegate as ReactRendererDelegate).builder = builder
            rendererDelegate.render(item)
        }
    }
}