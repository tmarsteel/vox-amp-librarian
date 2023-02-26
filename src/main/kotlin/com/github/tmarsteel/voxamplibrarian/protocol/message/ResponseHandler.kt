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
        fun <R : MessageToHost> singleMessage(factory: MidiProtocolMessage.Factory<R>): ResponseHandler<R> {
            return SingleMessageResponseHandler(factory)
        }

        fun <R> coroutine(block: suspend CoroutineResponseHandler<R>.Scope.() -> R): ResponseHandler<R> {
            return CoroutineResponseHandler(block)
        }
    }
}
