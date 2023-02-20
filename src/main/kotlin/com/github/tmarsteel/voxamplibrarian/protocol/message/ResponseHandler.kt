package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.BinaryInput

interface ResponseHandler<R> {
    /**
     * processes this message as part of the response
     * @return the response if it is available after processing, or `null` if more messages are needed
     */
    fun onMessage(payload: BinaryInput): MessageResult<R>

    /**
     * Called when reading the response is aborted
     */
    fun cancel()

    sealed class MessageResult<out R> {
        object MoreMessagesNeeded : MessageResult<Nothing>()
        class ResponseComplete<R>(val response: R) : MessageResult<R>()
    }

    companion object {
        fun <R : MessageToHost> singleMessage(factory: MidiProtocolMessage.Factory<R>): ResponseHandler<R> = SingleMessageHandler(factory)
        fun <R1, R2, RT> compound(h1: ResponseHandler<R1>, h2: ResponseHandler<R2>, composer: (R1, R2) -> RT): ResponseHandler<RT> = CompoundResponseHandler(
            listOf(h1, h2)
        ) { responseList ->
            @Suppress("UNCHECKED_CAST")
            composer(
                responseList[0] as R1,
                responseList[1] as R2,
            )
        }

        fun <R1, R2, R3, RT> compound(h1: ResponseHandler<R1>, h2: ResponseHandler<R2>, h3: ResponseHandler<R3>, composer: (R1, R2, R3) -> RT): ResponseHandler<RT> = CompoundResponseHandler(
            listOf(h1, h2)
        ) { responseList ->
            @Suppress("UNCHECKED_CAST")
            composer(
                responseList[0] as R1,
                responseList[1] as R2,
                responseList[2] as R3,
            )
        }
    }

    private class SingleMessageHandler<T : MessageToHost>(val factory: MidiProtocolMessage.Factory<T>) : ResponseHandler<T> {
        override fun onMessage(payload: BinaryInput): MessageResult<T> {
            return MessageResult.ResponseComplete(factory.parse(payload))
        }

        override fun cancel() {}
    }
}

private class CompoundResponseHandler<Response>(
    subHandlers: List<ResponseHandler<*>>,
    val responseBuilder: (List<Any?>) -> Response,
) : ResponseHandler<Response> {
    private val remainingSubHandlers = subHandlers.toMutableList()
    private val responses = mutableListOf<Any?>()

    override fun onMessage(payload: BinaryInput): ResponseHandler.MessageResult<Response> {
        val handler = remainingSubHandlers.first()
        when (val subResult = handler.onMessage(payload)) {
            is ResponseHandler.MessageResult.MoreMessagesNeeded -> {
                return subResult
            }
            is ResponseHandler.MessageResult.ResponseComplete<*> -> {
                responses.add(subResult.response)
                remainingSubHandlers.removeAt(0)
                if (remainingSubHandlers.isNotEmpty()) {
                    return ResponseHandler.MessageResult.MoreMessagesNeeded
                }

                return ResponseHandler.MessageResult.ResponseComplete(responseBuilder(responses))
            }
        }
    }

    override fun cancel() {
        remainingSubHandlers.forEach { it.cancel() }
    }
}