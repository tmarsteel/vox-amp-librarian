package com.github.tmarsteel.voxamplibrarian.protocol

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import com.github.tmarsteel.voxamplibrarian.computeIfAbsent
import com.github.tmarsteel.voxamplibrarian.protocol.message.*
import com.github.tmarsteel.voxamplibrarian.putIfAbsent
import kotlinx.coroutines.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class VoxVtxAmplifierClient(
    val midiDevice: MidiDevice,
    listener: (suspend (MessageToHost) -> Unit)? = null,
    messageFactories: List<MidiProtocolMessage.Factory<out MessageToHost>> = DEFAULT_MESSAGE_FACTORIES,
) {
    private val messageFactories: MutableSet<MidiProtocolMessage.Factory<out MessageToHost>> = HashSet(messageFactories)
    /**
     * maps [KClass.simpleName] of the awaited response to the coroutine to handle it
     */
    private val pendingExchanges = mutableMapOf<String, Continuation<MessageToHost>>()

    /**
     * When an exchange is initiated while another exchange with the same response type is already
     * underway ([pendingExchanges]), it gets queued here. The continuation does work of sending and
     * registering in [pendingExchanges].
     */
    private val queuedExchanges = mutableMapOf<String, ArrayDeque<Continuation<Unit>>>()
    private val listeners = mutableListOf<suspend (MessageToHost) -> Unit>()
    init {
        listener?.let(listeners::add)

        midiDevice.incomingSysExMessageHandler = { manufacturerId, payload ->
            GlobalScope.launch {
                this@VoxVtxAmplifierClient.onSysExMessageReceived(manufacturerId, payload)
            }
        }
    }

    private suspend fun send(message: MessageToAmp<*>) {
        midiDevice.sendSysExMessage(MANUFACTURER_ID, message::writeTo)
    }

    private suspend fun onSysExMessageReceived(manufacturerId: Byte, payload: BinaryInput) {
        if (manufacturerId != MANUFACTURER_ID) {
            return
        }

        var parsedMessage: MessageToHost? = null
        for (factory in messageFactories) {
            payload.seekToStart()
            val factoryResult = try {
                factory.parse(payload)
            } catch (ex: MessageParseException.PrefixNotRecognized) {
                continue
            }

            if (parsedMessage == null) {
                parsedMessage = factoryResult
            } else {
                throw AmbiguousMessageException(setOf(parsedMessage::class, factoryResult::class), payload)
            }
        }

        if (parsedMessage == null) {
            throw UnrecognizedMessageException(payload)
        }

        val messageType = parsedMessage::class.simpleName!!
        val exchangeContinuation = pendingExchanges[messageType]
        if (exchangeContinuation != null) {
            pendingExchanges.remove(messageType)
            GlobalScope.launch {
                scheduleNextExchange(messageType)
            }
            exchangeContinuation.resume(parsedMessage)
            return
        }

        listeners.forEach { it.invoke(parsedMessage) }
    }

    private fun scheduleNextExchange(responseType: String) {
        val scheduler = queuedExchanges[responseType]?.firstOrNull()
            ?: return

        scheduler.resume(Unit)
    }

    fun addListener(listener: (MessageToHost) -> Unit) {
        listeners.add(listener)
    }

    suspend fun <Response : MessageToHost> exchange(request: MessageToAmp<Response>, timeout: Duration = DEFAULT_TIMEOUT): Response {
        messageFactories.add(request.responseFactory)

        val responseType = request.responseFactory.type.simpleName!!
        // assure only one exchange per response type is in-flight at any given time
        if (responseType in pendingExchanges) {
            suspendCoroutine<Unit> { responseTypeFree ->
                val queue = queuedExchanges.computeIfAbsent(responseType) { _ -> ArrayDeque() }
                queue.addLast(responseTypeFree)
            }
        }

        val onResponseReceivedSubroutine = GlobalScope.async(start = CoroutineStart.DEFAULT) {
            return@async suspendCoroutine<MessageToHost> { responseAvailable ->
                check(pendingExchanges.putIfAbsent(responseType, responseAvailable)) {
                    "The response type is occupied by another exchange. This race condition should have been prevented."
                }
            }
        }
        send(request)
        val response = withTimeout(timeout) {
            onResponseReceivedSubroutine.await()
        }

        @Suppress("UNCHECKED_CAST")
        return response as Response
    }

    companion object {
        val DEFAULT_TIMEOUT = 1.seconds
        const val MANUFACTURER_ID: Byte = 0x42
        val DEFAULT_MESSAGE_FACTORIES = listOf<MidiProtocolMessage.Factory<out MessageToHost>>(
            AmpDialTurnedMessage.Companion,
            EffectDialTurnedMessage.Companion,
            NoiseReductionSensitivityChangedMessage.Companion,
            PedalActiveStateChangedMessage.Companion,
            ProgramSlotChangedMessage.Companion,
            SimulatedAmpModelChangedMessage.Companion,
            EffectPedalTypeChangedMessage.Companion,
            ErrorMessage.Companion,
        )
    }
}