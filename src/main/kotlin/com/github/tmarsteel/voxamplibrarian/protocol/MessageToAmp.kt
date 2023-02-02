package com.github.tmarsteel.voxamplibrarian.protocol

interface MessageToAmp : MidiProtocolMessage, ProtocolSerializable
interface HostInitiatedExchange<Response : MessageToHost> : MessageToAmp {
    val responseFactory: MidiProtocolMessage.Factory<Response>
}