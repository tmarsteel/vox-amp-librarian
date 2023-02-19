package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import com.github.tmarsteel.voxamplibrarian.requireEOF
import com.github.tmarsteel.voxamplibrarian.requirePrefix

abstract class ErrorMessage(
    val message: String,
) : MessageToHost {
    /** The amp did not recognize the command (or couldn't parse the message as the command it thinks its supposed to be) */
    class InvalidCommand : ErrorMessage("The command is not known / is encoded incorrectly")

    /** the amp could parse the command but one of the values in the command is invalid (out of range) */
    class InvalidValue : ErrorMessage("Invalid value")

    override fun toString() = message

    companion object : MidiProtocolMessage.Factory<ErrorMessage> {
        val PREFIX = byteArrayOf(0x30, 0x00, 0x01, 0x34)
        override val type = ErrorMessage::class
        override fun parse(fullMessage: BinaryInput): ErrorMessage {
            requirePrefix(fullMessage, PREFIX, ErrorMessage::class)
            val indicator = fullMessage.nextByte()
            val error = when (indicator.toInt()) {
                0x26 -> InvalidCommand()
                0x24 -> InvalidValue()
                else -> throw MessageParseException.PrefixNotRecognized(ErrorMessage::class)
            }
            requireEOF(fullMessage)

            return error
        }
    }
}