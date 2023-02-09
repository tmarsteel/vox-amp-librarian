@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")
package com.github.tmarsteel.voxamplibrarian

import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.Promise

class WebMidiPermissionDeniedException(override val cause: Throwable) : RuntimeException(cause)

sealed class MidiState {
    object NotSupported : MidiState()
    class PermissionDenied(val cause: Throwable) : MidiState() {
        fun reRequestPermission() {
            TODO()
        }
    }
    data class Available(
        val inputs: Map<String, MidiInput>,
        val outputs: Map<String, MidiOutput>,
    ) : MidiState()
}

val midiState: Flow<MidiState> = flow {
    val dynamicNavigator = window.navigator.asDynamic()
    if (dynamicNavigator.requestMIDIAccess === undefined) {
        emit(MidiState.NotSupported)
        return@flow
    }

    val midiAccessPromise: Promise<MidiAccess> = dynamicNavigator.requestMIDIAccess(object : MidiOptions {
        override var sysex = true
    })
    val midiAccess = try {
        suspendCancellableCoroutine<MidiAccess> { cont ->
            midiAccessPromise.then(
                onFulfilled = { cont.resume(it) },
                onRejected = { cont.resumeWithException(WebMidiPermissionDeniedException(it)) }
            )
        }
    }
    catch (e: WebMidiPermissionDeniedException) {
        emit(MidiState.PermissionDenied(e.cause))
        return@flow
    }

    val stateChangeEvents: Channel<MidiConnectionEvent> = Channel(Channel.BUFFERED, onBufferOverflow = BufferOverflow.SUSPEND)
    midiAccess.addStateChangedListener { event ->
        GlobalScope.launch {
            stateChangeEvents.send(event)
        }
    }
    emit(MidiState.Available(midiAccess.inputs, midiAccess.outputs))
    stateChangeEvents.consumeAsFlow().collect {
        emit(MidiState.Available(midiAccess.inputs, midiAccess.outputs))
    }
}

// ------ TYPINGS FROM @types/webmidi:2.0.6, adapted and fixed -------
external interface MidiOptions {
    var sysex: Boolean
}

typealias MidiInputMap = Map<String, MidiInput>

typealias MidiOutputMap = Map<String, MidiOutput>

external interface MidiAccess : EventTarget {
    var inputs: MidiInputMap
    var outputs: MidiOutputMap
    fun onstatechange(e: MidiConnectionEvent)
    var sysexEnabled: Boolean
}

fun MidiAccess.addStateChangedListener(listener: (MidiConnectionEvent) -> Unit) {
    addEventListener("statechange", listener.unsafeCast<(Event) -> Unit>())
}

external interface MidiPort : EventTarget {
    var id: String
    var manufacturer: String?
        get() = definedExternally
        set(value) = definedExternally
    var name: String?
        get() = definedExternally
        set(value) = definedExternally
    var type: String /* "input" | "output" */
    var version: String?
        get() = definedExternally
        set(value) = definedExternally
    var state: String /* "disconnected" | "connected" */
    var connection: String /* "open" | "closed" | "pending" */
    fun open(): Promise<MidiPort>
    fun close(): Promise<MidiPort>
}

external interface MidiInput : MidiPort {
    override var type: String /* "input" */
}

fun MidiInput.addMessageListener(listener: (MidiMessageEvent) -> Unit) {
    addEventListener("midimessage", listener.unsafeCast<(Event) -> Unit>())
}

external interface MidiOutput : MidiPort {
    override var type: String /* "output" */
    fun send(data: Array<Number>, timestamp: Number = definedExternally)
    fun send(data: Array<Number>)
    fun send(data: Array<Byte>, timestamp: Number = definedExternally)
    fun send(data: Array<Byte>)
    fun clear()
}

external interface MidiMessageEvent : Event {
    var receivedTime: Number
    var data: Array<Byte>
}

external interface MidiConnectionEvent : Event {
    var port: MidiPort
}