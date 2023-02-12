package com.github.tmarsteel.voxamplibrarian

/*
This file is supposed to completely contain/abstract the web/w3c/browser part of doing midi,
so the rest of the app can stay agnostic and work on other APIs (android, java, ...), too.
 */

import com.github.tmarsteel.voxamplibrarian.logging.LoggerFactory
import com.github.tmarsteel.voxamplibrarian.protocol.MidiDevice
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.Uint8Array
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.Promise

private class WebMidiPermissionDeniedException(override val cause: Throwable) : RuntimeException(cause)

private val logger = LoggerFactory["webmidi"]

private sealed class MidiState {
    object Initializing : MidiState()
    object NotSupported : MidiState()
    class PermissionDenied(val cause: Throwable) : MidiState()
    data class Available(
        val inputs: Map<String, MidiInput>,
        val outputs: Map<String, MidiOutput>,
    ) : MidiState() {
        val connectionStates: Map<String, Boolean> = (inputs.values + outputs.values).associate { it.id to (it.state == "connected") }
    }
}

private val midiState: Flow<MidiState> = flow {
    logger.trace("root webmidi flow is initializing")
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

    val initialState = MidiState.Available(midiAccess.inputs.toKotlinMap(), midiAccess.outputs.toKotlinMap())
    //emit(initialState)
    stateChangeEvents.consumeAsFlow()
        .runningFold(MidiStateFilterCarry(initialState, true)) { filterState, event ->
            val currentState = MidiState.Available(midiAccess.inputs.toKotlinMap(), midiAccess.outputs.toKotlinMap())
            val previousPortConnectionState =  filterState.previousState.connectionStates[event.port.id]
            val currentPortConnectionState = event.port.state == "connected"
            val actualChanges = previousPortConnectionState != currentPortConnectionState
            logger.trace("state change event for port ${event.port.id}: previous: $previousPortConnectionState, now: $currentPortConnectionState, changed = $actualChanges")
            MidiStateFilterCarry(currentState, actualChanges)
        }
        .filter { it.actualChanges }
        .map { it.previousState }
        .collect(this@flow::emit)
}.shareIn(GlobalScope, SharingStarted.Lazily, 0)

private class MidiStateFilterCarry(
    val previousState: MidiState.Available,
    val actualChanges: Boolean,
)

val VOX_AMP_MIDI_DEVICE: Flow<MidiDevice?> = midiState
    .runningFold<MidiState, WebMidiVoxVtxDevice?>(null) { currentDevice, currentMidiState ->
        when (currentMidiState) {
            is MidiState.Initializing -> return@runningFold null
            is MidiState.PermissionDenied,
            MidiState.NotSupported -> {
                currentDevice?.close()
                window.alert("Midi not available: ${currentMidiState::class.simpleName}")
                return@runningFold null
            }
            is MidiState.Available -> {
                if (currentDevice != null) {
                    if (!currentDevice.isConnected) {
                        currentDevice.close()
                        return@runningFold null
                    }

                    return@runningFold currentDevice
                }

                WebMidiVoxVtxDevice.tryBuildFrom(currentMidiState)
            }
        }
    }
    .shareIn(GlobalScope, SharingStarted.Lazily, 0)

private class WebMidiVoxVtxDevice private constructor(val input: MidiInput, val output: MidiOutput) : MidiDevice {
    override suspend fun sendSysExMessage(manufacturerId: Byte, writer: (BinaryOutput) -> Unit) {
        val binaryOutput = BufferedBinaryOutput()
        binaryOutput.write(0xf0.toByte())
        binaryOutput.write(manufacturerId)
        writer(binaryOutput)
        binaryOutput.write(0xf7.toByte())
        val rawData = binaryOutput.contentAsArrayOfUnsignedInts()
        logger.debug("> ${rawData.hex()}")
        output.send(rawData)
    }

    override lateinit var incomingSysExMessageHandler: (manufacturerId: Byte, payload: BinaryInput) -> Unit

    init {
        input.onmidimessage = onmidimessage@{ messageEvent ->
            logger.debug("< ${messageEvent.data.toByteArray().hex()}")

            if (!this::incomingSysExMessageHandler.isInitialized) {
                console.warn("Dropping incoming message because no handler is registered.", messageEvent)
                return@onmidimessage
            }

            if (messageEvent.data.length < 3) {
                return@onmidimessage
            }
            if (messageEvent.data[0] != 0xf0) {
                return@onmidimessage
            }
            if (messageEvent.data[messageEvent.data.length - 1] != 0xf7) {
                return@onmidimessage
            }

            val manufacturerId = messageEvent.data[1].toByte()

            this.incomingSysExMessageHandler(manufacturerId, ByteArrayBinaryInput(messageEvent.data.toByteArray(), 2, messageEvent.data.length - 2))
        }
    }

    val isConnected: Boolean
        get() = input.state == "connected" && output.state == "connected"

    suspend fun close() {
        await(input.close())
        await(output.close())
    }

    companion object {
        private val logger = LoggerFactory["vtx-midi"]
        fun tryBuildFrom(midiState: MidiState.Available): WebMidiVoxVtxDevice? {
            val input = midiState.inputs.entries.singleOrNull { (_, input) -> input.isVtxAmp }
                ?: return null

            val output = midiState.outputs.entries.singleOrNull { (_, output) -> output.isVtxAmp }
                ?: return null

            return WebMidiVoxVtxDevice(input.value, output.value)
        }

        private val MidiPort.isVtxAmp: Boolean
            get() = manufacturer?.lowercase() == "korg, inc." && name == "Valvetronix X"
    }
}

private fun BufferedBinaryOutput.contentAsArrayOfUnsignedInts(): Array<Int> {
    val asInput = copyToInput()
    return Array(asInput.bytesRemaining) { asInput.nextByte().toUByte().toInt() }
}

private fun <K, V> JsMap<K, V>.toKotlinMap(): Map<K, V> {
    val map = HashMap<K, V>(size)
    forEach { value, key ->
        map[key] = value
    }

    return map
}

@Suppress("NOTHING_TO_INLINE")
private inline operator fun Uint8Array.get(index: Int): Int = asDynamic()[index]

private fun Uint8Array.toByteArray(): ByteArray {
    return ByteArray(length) { (this[it] as Number).toByte() }
}

// ------ TYPINGS FROM @types/webmidi:2.0.6, adapted and fixed -------
private external interface JsMap<K, V> {
    val size: Int
    fun forEach(callback: (V, K) -> Unit)
}

private external interface MidiOptions {
    var sysex: Boolean
}

private typealias MidiInputMap = JsMap<String, MidiInput>

private typealias MidiOutputMap = JsMap<String, MidiOutput>

@Suppress("INTERFACE_WITH_SUPERCLASS")
private external interface MidiAccess : EventTarget {
    var inputs: MidiInputMap
    var outputs: MidiOutputMap
    fun onstatechange(e: MidiConnectionEvent)
    var sysexEnabled: Boolean
}

private fun MidiAccess.addStateChangedListener(listener: (MidiConnectionEvent) -> Unit) {
    addEventListener("statechange", listener.unsafeCast<(Event) -> Unit>())
}

@Suppress("INTERFACE_WITH_SUPERCLASS")
private external interface MidiPort : EventTarget {
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

private external interface MidiInput : MidiPort {
    var onmidimessage: ((MidiMessageEvent) -> Unit)?
    override var type: String /* "input" */
}

private external interface MidiOutput : MidiPort {
    override var type: String /* "output" */
    fun send(data: Array<Number>, timestamp: Number = definedExternally)
    fun send(data: Array<Number>)
    fun send(data: Array<Int>, timestamp: Number = definedExternally)
    fun send(data: Array<Int>)
    fun clear()
}

@Suppress("INTERFACE_WITH_SUPERCLASS")
private external interface MidiMessageEvent : Event {
    var receivedTime: Number
    var data: Uint8Array
}

@Suppress("INTERFACE_WITH_SUPERCLASS")
private external interface MidiConnectionEvent : Event {
    var port: MidiPort
}