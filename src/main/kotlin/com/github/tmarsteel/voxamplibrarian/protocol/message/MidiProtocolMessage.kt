package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import kotlin.reflect.KClass

interface MidiProtocolMessage {
    interface Factory<T : MidiProtocolMessage> {
        val type: KClass<T>

        /**
         * @throws MessageParseException
         */
        fun parse(fullMessage: BinaryInput): T
    }
}