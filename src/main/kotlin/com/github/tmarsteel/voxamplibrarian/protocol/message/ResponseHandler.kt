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
    }

    private class SingleMessageHandler<T : MessageToHost>(val factory: MidiProtocolMessage.Factory<T>) : ResponseHandler<T> {
        override fun onMessage(payload: BinaryInput): MessageResult<T> {
            return MessageResult.ResponseComplete(factory.parse(payload))
        }

        override fun cancel() {}
    }
}