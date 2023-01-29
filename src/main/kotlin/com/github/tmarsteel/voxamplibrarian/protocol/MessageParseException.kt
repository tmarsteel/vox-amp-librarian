package com.github.tmarsteel.voxamplibrarian.protocol

import kotlin.reflect.KClass

sealed class MessageParseException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause) {
    class PrefixNotRecognized(val targetType: KClass<out MidiProtocolMessage>) : MessageParseException("The message doesn't start with the right prefix for a ${targetType.simpleName}")
    class InvalidMessage(message: String, cause: Throwable? = null) : MessageParseException(message, cause)
}