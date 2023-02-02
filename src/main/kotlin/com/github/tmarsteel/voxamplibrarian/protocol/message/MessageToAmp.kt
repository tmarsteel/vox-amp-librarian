package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.protocol.ProtocolSerializable

interface MessageToAmp<Response : MessageToHost> : MidiProtocolMessage, ProtocolSerializable {
    val responseFactory: MidiProtocolMessage.Factory<Response>
}

interface CommandWithoutResponse : MessageToAmp<GenericAcknowledgement> {
    override val responseFactory: MidiProtocolMessage.Factory<GenericAcknowledgement>
        get() = GenericAcknowledgement
}