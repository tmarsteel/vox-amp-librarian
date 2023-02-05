package com.github.tmarsteel.voxamplibrarian.protocol

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import com.github.tmarsteel.voxamplibrarian.preview
import com.github.tmarsteel.voxamplibrarian.protocol.message.MidiProtocolMessage
import kotlin.reflect.KClass

class AmbiguousMessageException(
    val recognizedTypes: Set<KClass<out MidiProtocolMessage>>,
    message: BinaryInput,
) : RuntimeException(
    run {
        val types = recognizedTypes.joinToString(transform = { it.simpleName ?: "<unknown>" })
        "Received ambiguous message (recognized as $types): ${message.preview()}"
    }
)

