package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.protocol.ProtocolSerializable

interface MessageToAmp<Response> : MidiProtocolMessage, ProtocolSerializable {
    fun createResponseHandler(): ResponseHandler<Response>
}

interface CommandWithoutResponse : MessageToAmp<GenericAcknowledgement> {
    override fun createResponseHandler() = ResponseHandler.singleMessage(GenericAcknowledgement)
}