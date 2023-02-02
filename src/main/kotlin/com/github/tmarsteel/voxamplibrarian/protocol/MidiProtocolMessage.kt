package com.github.tmarsteel.voxamplibrarian.protocol

interface MidiProtocolMessage {
    interface Factory<T : MidiProtocolMessage> {
        /**
         * @throws MessageParseException
         */
        fun parse(fullMessage: BinaryInput): T
    }
}