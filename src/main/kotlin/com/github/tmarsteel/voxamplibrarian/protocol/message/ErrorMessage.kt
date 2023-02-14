package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import com.github.tmarsteel.voxamplibrarian.requireEOF
import com.github.tmarsteel.voxamplibrarian.requirePrefix

class ErrorMessage : MessageToHost {
    companion object : MidiProtocolMessage.Factory<ErrorMessage> {
        val PREFIX = byteArrayOf(0x30, 0x00, 0x01, 0x34, 0x26)
        override val type = ErrorMessage::class
        override fun parse(fullMessage: BinaryInput): ErrorMessage {
            requirePrefix(fullMessage, PREFIX, ErrorMessage::class)
            requireEOF(fullMessage)

            return ErrorMessage()
        }
    }
}