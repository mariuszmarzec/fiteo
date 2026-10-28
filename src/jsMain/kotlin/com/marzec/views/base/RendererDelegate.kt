package com.marzec.views.base

import react.ChildrenBuilder

interface RendererDelegate {

    fun render(renderer: Renderer, item: ViewItem)
}

abstract class ReactRendererDelegate : RendererDelegate {

    lateinit var builder: ChildrenBuilder

    override fun render(renderer: Renderer, item: ViewItem): Unit = builder.render(renderer as ReactRenderer, item)

    abstract fun ChildrenBuilder.render(renderer: Renderer, item: ViewItem)
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
            delegates.first { item.check(it) }.render(this, item)
        }
    }
}

class ReactRenderer : Renderer {

    lateinit var builder: ChildrenBuilder

    private val delegates = mutableListOf<RendererDelegate>()

    override fun add(delegate: RendererDelegate): Renderer {
        delegates.add(delegate)
        return this
    }

    override fun render(items: List<ViewItem>) {
        render(items, builder)
    }

    fun render(items: List<ViewItem>, builder: ChildrenBuilder = this.builder) {
        items.forEach { item ->
            val rendererDelegate = delegates.firstOrNull { item.check(it) }
                ?: throw NoSuchElementException("No delegates for item: ${item::class.simpleName}")
            (rendererDelegate as ReactRendererDelegate).builder = builder
            rendererDelegate.render(this, item)
        }
    }
}
