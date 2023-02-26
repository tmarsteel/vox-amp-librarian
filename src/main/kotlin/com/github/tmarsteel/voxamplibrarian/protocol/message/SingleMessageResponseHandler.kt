package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.BinaryInput

class SingleMessageResponseHandler<T : MessageToHost> internal constructor(val factory: MidiProtocolMessage.Factory<T>) :
    ResponseHandler<T> {
    override fun onMessage(payload: BinaryInput): ResponseHandler.MessageResult<T> {
        return ResponseHandler.MessageResult.ResponseComplete(factory.parse(payload))
    }

    override fun cancel() {}
}