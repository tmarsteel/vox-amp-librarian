package com.github.tmarsteel.voxamplibrarian.reactapp

import kotlinx.browser.window
import kotlinx.js.jso
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import react.dom.events.MouseEventHandler
import react.dom.events.WheelEvent
import react.dom.html.HTMLAttributes

object FullyConsumedWheelEventAssist {
    private val documentWheelListener: (Event) -> Unit = { event ->
        event.preventDefault()
    }
    private val documentWheelListenerOptions: dynamic = jso { capture = true; passive = false; }

    fun <T : Element> HTMLAttributes<T>.registerFullyConsumingWheelEventListener(
        onMouseEnter: MouseEventHandler<T> = {},
        onMouseLeave: MouseEventHandler<T> = {},
        onWheelCapture: (WheelEvent<T>) -> Unit
    ) {
        this.onMouseEnter = { event ->
            window.document.addEventListener("wheel", documentWheelListener, documentWheelListenerOptions)
            onMouseEnter(event)
        }
        this.onMouseLeave = { event ->
            window.document.removeEventListener("wheel", documentWheelListener, documentWheelListenerOptions)
            onMouseLeave(event)
        }
        this.onWheelCapture = { event ->
            onWheelCapture(event)
        }
    }
}