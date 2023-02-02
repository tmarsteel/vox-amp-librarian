package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.BinaryInput

interface MidiProtocolMessage {
    interface Factory<T : MidiProtocolMessage> {
        /**
         * @throws MessageParseException
         */
        fun parse(fullMessage: BinaryInput): T
    }
}