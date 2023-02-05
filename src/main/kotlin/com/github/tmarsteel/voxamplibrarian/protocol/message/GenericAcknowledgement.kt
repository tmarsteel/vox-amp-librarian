package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import com.github.tmarsteel.voxamplibrarian.requireEOF
import com.github.tmarsteel.voxamplibrarian.requirePrefix

class GenericAcknowledgement : MessageToHost {
    companion object : MidiProtocolMessage.Factory<GenericAcknowledgement> {
        val PREFIX = byteArrayOf(0x30, 0x00, 0x01, 0x34, 0x23)
        override val type = GenericAcknowledgement::class
        override fun parse(fullMessage: BinaryInput): GenericAcknowledgement {
            requirePrefix(fullMessage, PREFIX, GenericAcknowledgement::class)
            requireEOF(fullMessage)

            return GenericAcknowledgement()
        }
    }
}