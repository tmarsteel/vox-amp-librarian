package com.github.tmarsteel.voxamplibrarian.protocol

interface MessageToAmp<Response : MessageToHost> : MidiProtocolMessage, ProtocolSerializable {
    val responseFactory: MidiProtocolMessage.Factory<Response>
}

interface CommandWithoutResponse : MessageToAmp<GenericAcknowledgement> {
    override val responseFactory: MidiProtocolMessage.Factory<GenericAcknowledgement>
        get() = GenericAcknowledgement.Companion
}