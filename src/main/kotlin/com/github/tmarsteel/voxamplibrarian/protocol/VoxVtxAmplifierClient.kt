package com.github.tmarsteel.voxamplibrarian.protocol

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import com.github.tmarsteel.voxamplibrarian.CoroutineLock
import com.github.tmarsteel.voxamplibrarian.logging.LoggerFactory
import com.github.tmarsteel.voxamplibrarian.protocol.message.*
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class VoxVtxAmplifierClient(
    val midiDevice: MidiDevice,
    listener: (suspend (MessageToHost) -> Unit)? = null,
    private val messageFactories: List<MidiProtocolMessage.Factory<MessageToHost>> = DEFAULT_MESSAGE_FACTORIES,
) {
    private val logger = LoggerFactory["protocol-mgmt"]

    private val currentExchange: AtomicRef<ExchangeData<MessageToHost>?> = atomic(null)
    private val exchangeLock = CoroutineLock("vox-vtx-exchange")

    private suspend fun onSysExMessageReceived(manufacturerId: Byte, payload: BinaryInput) {
        if (manufacturerId != MANUFACTURER_ID) {
            return
        }

        val initialCurrentExchange = currentExchange.value
        if (initialCurrentExchange != null) {
            try {
                val exchangeResponse = initialCurrentExchange.messageFactory.parse(payload)
                if (currentExchange.compareAndSet(initialCurrentExchange, null)) {
                    GlobalScope.launch {
                        initialCurrentExchange.continuation.resume(exchangeResponse)
                    }
                } else {
                    logger.warn("Ignoring possible exchange response (${exchangeResponse::class.simpleName}) because there has been a race condition in consuming the response.")
                }
                return
            }
            catch (ex: MessageParseException.PrefixNotRecognized) {
                payload.seekToStart()
                try {
                    val error = ErrorMessage.parse(payload)
                    logger.error("Received error response from device: $error")
                    if (currentExchange.compareAndSet(initialCurrentExchange, null)) {
                        GlobalScope.launch {
                            initialCurrentExchange.continuation.resumeWithException(
                                MessageNotAcknowledgedException(initialCurrentExchange.request, error)
                            )
                        }
                    } else {
                        logger.warn("Ignoring error exchange response because there has been a race condition in consuming the response.")
                    }
                    return
                }
                catch (ex: MessageParseException.PrefixNotRecognized) {
                    // message not related to exchange, parse as event
                }
            }
            catch (ex: MessageParseException) {
                if (currentExchange.compareAndSet(initialCurrentExchange, null)) {
                    GlobalScope.launch {
                        initialCurrentExchange.continuation.resumeWithException(ex)
                    }
                } else {
                    logger.warn("Ignoring possible exchange failure because there has been a race condition in consuming the response.", ex)
                }
                return
            }
            finally {
                payload.seekToStart()
            }
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

        listeners.forEach { it.invoke(parsedMessage) }
    }

    suspend fun <Response : MessageToHost> exchange(request: MessageToAmp<Response>, timeout: Duration = DEFAULT_TIMEOUT): Response {
        return exchangeLock.withLock {
            val onResponseReceivedSubroutine = GlobalScope.async(start = CoroutineStart.DEFAULT) {
                return@async suspendCoroutine<MessageToHost> { responseAvailable ->
                    val exchangeData = ExchangeData(request, request.responseFactory, responseAvailable) as ExchangeData<MessageToHost>
                    check(currentExchange.compareAndSet(null, exchangeData)) {
                        "currentExchange is set while lock was not held"
                    }
                }
            }

            try {
                send(request)
                val response = withTimeout(timeout) {
                    @Suppress("UNCHECKED_CAST")
                    onResponseReceivedSubroutine.await() as Response
                }
                return@withLock response
            }
            finally {
                currentExchange.value = null
            }
        }
    }

    private suspend fun send(message: MessageToAmp<*>) {
        midiDevice.sendSysExMessage(MANUFACTURER_ID, message::writeTo)
    }

    private val listeners = mutableListOf<suspend (MessageToHost) -> Unit>()
    init {
        listener?.let(listeners::add)

        midiDevice.incomingSysExMessageHandler = { manufacturerId, payload ->
            GlobalScope.launch {
                onSysExMessageReceived(manufacturerId, payload)
            }
        }
    }
    fun addListener(listener: (MessageToHost) -> Unit) {
        listeners.add(listener)
    }

    private class ExchangeData<Response : MessageToHost>(
        val request: MessageToAmp<Response>,
        val messageFactory: MidiProtocolMessage.Factory<Response>,
        val continuation: Continuation<Response>,
    )

    companion object {
        val DEFAULT_TIMEOUT = 1.seconds
        const val MANUFACTURER_ID: Byte = 0x42
        val DEFAULT_MESSAGE_FACTORIES = listOf<MidiProtocolMessage.Factory<MessageToHost>>(
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