package com.github.tmarsteel.voxamplibrarian.reactapp

import com.github.tmarsteel.voxamplibrarian.logging.LoggerFactory
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import react.dom.DOMAttributes

object GlobalMouseMoveAssist {
    private val logger = LoggerFactory["global-mousemove"]
    private var currentInteraction: Interaction? = null

    fun DOMAttributes<out HTMLElement>.registerGlobalDragHandler(
        onDragStart: (MouseEvent) -> Unit,
        onDrag: (MouseEvent) -> Unit,
        onDragEnd: (MouseEvent) -> Unit,
    ) {
        onMouseDown = { event ->
            if (currentInteraction != null) {
                logger.warn("got two mousedowns without a mouseup")
            } else {
                register()
            }

            onDragStart(event.nativeEvent)
            currentInteraction = Interaction(onDrag, onDragEnd)
            undefined
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
        window.document.body?.classList?.add("during-global-drag")
        window.addEventListener("mousemove", globalMoveHandler.unsafeCast<(Event) -> Unit>())
        window.addEventListener("mouseup", globalUpHandler.unsafeCast<(Event) -> Unit>())
    }

    private fun deregister() {
        window.document.body?.classList?.remove("during-global-drag")
        window.removeEventListener("mousemove", globalMoveHandler.unsafeCast<(Event) -> Unit>())
        window.removeEventListener("mouseup", globalUpHandler.unsafeCast<(Event) -> Unit>())
    }

    private class Interaction(
        val moveHandler: (MouseEvent) -> Unit,
        val upHandler: (MouseEvent) -> Unit,
    )
}