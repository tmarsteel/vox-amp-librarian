package com.github.tmarsteel.voxamplibrarian.protocol

interface MidiProtocolMessage {
    fun writeTo(out: BinaryOutput)

    interface Factory<T : MidiProtocolMessage> {
        /**
         * @throws MessageParseException
         */
        fun parse(fullMessage: ByteArray): T
    }
}