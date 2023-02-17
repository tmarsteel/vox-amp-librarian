package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import com.github.tmarsteel.voxamplibrarian.requireEOF
import com.github.tmarsteel.voxamplibrarian.requirePrefix

abstract class ErrorMessage(
    val message: String,
) : MessageToHost {
    class GeneralError : ErrorMessage("Unknown error")
    class InvalidValue : ErrorMessage("Invalid value")

    override fun toString() = message

    companion object : MidiProtocolMessage.Factory<ErrorMessage> {
        val PREFIX = byteArrayOf(0x30, 0x00, 0x01, 0x34)
        override val type = ErrorMessage::class
        override fun parse(fullMessage: BinaryInput): ErrorMessage {
            requirePrefix(fullMessage, PREFIX, ErrorMessage::class)
            val indicator = fullMessage.nextByte()
            val error = when (indicator.toInt()) {
                0x26 -> GeneralError()
                0x24 -> InvalidValue()
                else -> throw MessageParseException.PrefixNotRecognized(ErrorMessage::class)
            }
            requireEOF(fullMessage)

            return error
        }
    }
}