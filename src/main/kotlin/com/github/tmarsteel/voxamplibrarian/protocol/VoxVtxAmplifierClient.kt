package com.github.tmarsteel.voxamplibrarian.protocol

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import com.github.tmarsteel.voxamplibrarian.logging.LoggerFactory
import com.github.tmarsteel.voxamplibrarian.protocol.message.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
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
    private var closed = false

    private val logger = LoggerFactory["protocol-mgmt"]

    private val listeners = mutableListOf<suspend (MessageToHost) -> Unit>()
    init {
        listener?.let(listeners::add)
    }

    private val noneExchangeState = NoneExchangeState()
    private var exchangeState: ExchangeState = noneExchangeState

    init {
        midiDevice.incomingSysExMessageHandler = handle@{ manufacturerId, payload ->
            if (closed) {
                return@handle
            }

            GlobalScope.launch {
                if (manufacturerId != MANUFACTURER_ID) {
                    logger.trace("Ignoring SysEx message because the manufacturer id doesn't match (got $manufacturerId, expected $MANUFACTURER_ID)")
                }

                exchangeState = exchangeState.onSysExMessageReceived(payload)
            }
        }
    }

    suspend fun <Response> exchange(request: MessageToAmp<Response>, timeout: Duration = DEFAULT_TIMEOUT): Response {
        return exchange(Exchange.of(request), timeout)
    }

    suspend fun <Response> exchange(exchange: Exchange<Response>, timeout: Duration = DEFAULT_TIMEOUT): Response {
        return withTimeout(timeout) {
            suspendCoroutine<Response> { responseAvailable ->
                exchangeState = exchangeState.doExchange(exchange, responseAvailable)
            }
        }
    }

    private suspend fun send(message: MessageToAmp<*>) {
        midiDevice.sendSysExMessage(MANUFACTURER_ID, message::writeTo)
    }

    fun addListener(listener: (MessageToHost) -> Unit) {
        listeners.add(listener)
    }

    fun close() {
        if (closed) {
            return
        }
        closed = true
    }

    private abstract inner class ExchangeState {
        abstract suspend fun onSysExMessageReceived(payload: BinaryInput): ExchangeState
        abstract fun <R> doExchange(exchange: Exchange<R>, responseAvailable: Continuation<R>): ExchangeState
        abstract fun cancel(): ExchangeState
    }

    private inner class NoneExchangeState : VoxVtxAmplifierClient.ExchangeState() {
        override suspend fun onSysExMessageReceived(payload: BinaryInput): ExchangeState {
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
            return this
        }

        override fun <R> doExchange(exchange: Exchange<R>, responseAvailable: Continuation<R>): ExchangeState {
            return OngoingExchangeState(exchange, responseAvailable)
        }

        override fun cancel() = this
    }

    private inner class OngoingExchangeState<R>(
        val exchange: Exchange<R>,
        private val responseAvailable: Continuation<R>,
        queuedExchanges: List<QueuedExchange<*>> = listOf(),
    ): VoxVtxAmplifierClient.ExchangeState() {
        constructor(nextExchange: QueuedExchange<R>, queuedExchanges: List<QueuedExchange<*>>) : this(
            nextExchange.exchange,
            nextExchange.responseAvailable,
            queuedExchanges,
        )

        private val responseHandler: ResponseHandler<R> = exchange.createResponseHandler()
        private var responseAvailableResumed = false
        private var queuedExchanges = queuedExchanges.toMutableList()
        private val sendJob = GlobalScope.launch {
            try {
                for (message in exchange.messagesToSend) {
                    send(message)
                }
            }
            catch (ex: Throwable) {
                if (!responseAvailableResumed) {
                    responseAvailableResumed = true
                    responseAvailable.resumeWithException(ex)
                }
            }
        }

        override suspend fun onSysExMessageReceived(payload: BinaryInput): ExchangeState {
            if (responseAvailableResumed) {
                return this@VoxVtxAmplifierClient.noneExchangeState
            }

            try {
                val error = ErrorMessage.parse(payload)
                responseAvailableResumed = true
                responseAvailable.resumeWithException(ExchangeNotAcknowledgedException(exchange, error))
                return getNextExchangeState()
            }
            catch (ex: MessageParseException.PrefixNotRecognized) {
                // not an error, great
            }
            finally {
                payload.seekToStart()
            }

            when (val handlingResult = responseHandler.onMessage(payload)) {
                is ResponseHandler.MessageResult.ResponseComplete -> {
                    responseAvailableResumed = true
                    responseAvailable.resume(handlingResult.response)
                    return getNextExchangeState()
                }
                is ResponseHandler.MessageResult.MoreMessagesNeeded -> {
                    return this
                }
            }
        }

        override fun <R> doExchange(exchange: Exchange<R>, responseAvailable: Continuation<R>): ExchangeState {
            queuedExchanges.add(QueuedExchange(exchange, responseAvailable))
            return this
        }

        override fun cancel(): ExchangeState {
            sendJob.cancel()
            return getNextExchangeState()
        }

        private fun getNextExchangeState(): ExchangeState {
            if (queuedExchanges.isEmpty()) {
                return this@VoxVtxAmplifierClient.noneExchangeState
            }

            val nextExchange = queuedExchanges.first()
            val leftoverExchanges = queuedExchanges.subList(1, queuedExchanges.size)
            return OngoingExchangeState(nextExchange, leftoverExchanges)
        }
    }

    private class QueuedExchange<R>(
        val exchange: Exchange<R>,
        val responseAvailable: Continuation<R>,
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