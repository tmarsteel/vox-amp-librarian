package com.github.tmarsteel.voxamplibrarian.reactapp

import kotlinx.browser.window
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import react.dom.DOMAttributes

object GlobalMouseMoveAssist {
    private var currentInteraction: Interaction? = null

    fun DOMAttributes<*>.registerGlobalDragHandler(
        onDragStart: (MouseEvent) -> Unit,
        onDrag: (MouseEvent) -> Unit,
        onDragEnd: (MouseEvent) -> Unit,
    ) {
        onMouseDown = { event ->
            console.log("foo")
            check(currentInteraction == null) { "got a two mousedowns without a mouseup" }

            register()
            onDragStart(event.nativeEvent)
            console.log("bar")
            currentInteraction = Interaction(onDrag, onDragEnd)
        }
    }

    private val globalMoveHandler: (MouseEvent) -> Unit = { event ->
        currentInteraction?.moveHandler?.invoke(event)
    }

    private val globalUpHandler: (MouseEvent) -> Unit = { event ->
        val tmpInteraction = currentInteraction
        currentInteraction = null
        deregister()
        tmpInteraction?.upHandler?.invoke(event)
    }

    private fun register() {
        window.addEventListener("mousemove", globalMoveHandler.unsafeCast<(Event) -> Unit>())
        window.addEventListener("mouseup", globalUpHandler.unsafeCast<(Event) -> Unit>())
    }

    private fun deregister() {
        window.removeEventListener("mousemove", globalMoveHandler.unsafeCast<(Event) -> Unit>())
        window.removeEventListener("mouseup", globalUpHandler.unsafeCast<(Event) -> Unit>())
    }

    private class Interaction(
        val moveHandler: (MouseEvent) -> Unit,
        val upHandler: (MouseEvent) -> Unit,
    )
}