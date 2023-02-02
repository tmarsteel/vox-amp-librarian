package com.github.tmarsteel.voxamplibrarian.protocol

class GenericAcknowledgement : MessageToHost {
    companion object : MidiProtocolMessage.Factory<GenericAcknowledgement> {
        val PREFIX = byteArrayOf(0x30, 0x00, 0x01, 0x34, 0x23)
        override fun parse(fullMessage: BinaryInput): GenericAcknowledgement {
            requirePrefix(fullMessage, PREFIX, GenericAcknowledgement::class)
            requireEOF(fullMessage)

            return GenericAcknowledgement()
        }
    }
}