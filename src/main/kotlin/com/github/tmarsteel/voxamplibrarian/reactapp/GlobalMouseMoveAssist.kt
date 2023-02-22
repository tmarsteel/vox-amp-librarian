package com.github.tmarsteel.voxamplibrarian.reactapp

import com.github.tmarsteel.voxamplibrarian.logging.LoggerFactory
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.Touch
import org.w3c.dom.TouchEvent
import org.w3c.dom.asList
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import react.dom.DOMAttributes

object GlobalMouseMoveAssist {
    private val logger = LoggerFactory["global-mousemove"]
    private var currentInteraction: Interaction? = null

    fun DOMAttributes<out HTMLElement>.registerGlobalDragHandler(
        onDragStart: (MouseOrSingleFingerTouchEvent) -> Unit,
        onDrag: (MouseOrSingleFingerTouchEvent) -> Unit,
        onDragEnd: (MouseOrSingleFingerTouchEvent) -> Unit,
    ) {
        val startHandler: (MouseOrSingleFingerTouchEvent) -> Unit = { event ->
            if (currentInteraction != null) {
                logger.warn("got two mousedowns without a mouseup")
            } else {
                register()
            }

            onDragStart(event)
            currentInteraction = Interaction(onDrag, onDragEnd)
            undefined
        }
        onMouseDown = { reactEvent -> startHandler(MouseOrSingleFingerTouchEvent(reactEvent.nativeEvent)) }
        onTouchStart = touchStart@{ reactEvent ->
            MouseOrSingleFingerTouchEvent.from(reactEvent.nativeEvent)?.let(startHandler)
        }
    }

    private val globalMoveHandler: (Event) -> Unit = move@{ event ->
        MouseOrSingleFingerTouchEvent.from(event)?.let { abstractEvent ->
            currentInteraction?.moveHandler?.invoke(abstractEvent)
        }
    }

    private val globalUpHandler: (MouseEvent) -> Unit = stop@{ event ->
        val tmpInteraction = currentInteraction
        currentInteraction = null
        deregister()

        MouseOrSingleFingerTouchEvent.from(event)?.let { abstractEvent ->
            tmpInteraction?.upHandler?.invoke(abstractEvent)
        }
    }

    private fun register() {
        window.document.body?.classList?.add("during-global-drag")
        window.addEventListener("mousemove", globalMoveHandler.unsafeCast<(Event) -> Unit>())
        window.addEventListener("touchmove", globalMoveHandler.unsafeCast<(Event) -> Unit>())
        window.addEventListener("mouseup", globalUpHandler.unsafeCast<(Event) -> Unit>())
        window.addEventListener("touchend", globalUpHandler.unsafeCast<(Event) -> Unit>())
    }

    private fun deregister() {
        window.document.body?.classList?.remove("during-global-drag")
        window.removeEventListener("mousemove", globalMoveHandler.unsafeCast<(Event) -> Unit>())
        window.removeEventListener("touchmove", globalMoveHandler.unsafeCast<(Event) -> Unit>())
        window.removeEventListener("mouseup", globalUpHandler.unsafeCast<(Event) -> Unit>())
        window.removeEventListener("touchend", globalUpHandler.unsafeCast<(Event) -> Unit>())
    }

    private class Interaction(
        val moveHandler: (MouseOrSingleFingerTouchEvent) -> Unit,
        val upHandler: (MouseOrSingleFingerTouchEvent) -> Unit,
    )

    data class MouseOrSingleFingerTouchEvent(
        val screenX: Int,
        val screenY: Int,
    ) {
        constructor(event: MouseEvent) : this(event.screenX, event.screenY)
        constructor(touch: Touch) : this(touch.screenX, touch.screenY)

        companion object {
            fun from(event: Event): MouseOrSingleFingerTouchEvent? {
                if (event.asDynamic().touches !== undefined) {
                    return (event as TouchEvent).touches.asList().singleOrNull()?.let { touch ->
                        MouseOrSingleFingerTouchEvent(touch)
                    }
                }

                if (event.asDynamic().screenY !== undefined && event.asDynamic().screenX !== undefined) {
                    return (event as MouseEvent).let(::MouseOrSingleFingerTouchEvent)
                }

                return null
            }
        }
    }
}