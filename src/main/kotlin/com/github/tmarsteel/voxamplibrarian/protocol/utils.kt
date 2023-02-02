package com.github.tmarsteel.voxamplibrarian.protocol

import kotlin.reflect.KClass

private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
    if (prefix.size > this.size) {
        return false
    }

    for (i in prefix.indices) {
        if (this[i] != prefix[i]) {
            return false
        }
    }

    return true
}

internal fun requirePrefix(data: BinaryInput, prefix: ByteArray, targetType: KClass<out MidiProtocolMessage>) {
    if (data.bytesRemaining < prefix.size) {
        throw MessageParseException.PrefixNotRecognized(targetType)
    }

    for (prefixByte in prefix) {
        if (data.nextByte() != prefixByte) {
            throw MessageParseException.PrefixNotRecognized(targetType)
        }
    }
}

internal fun Byte.hex(): String = "0x" + toString(16).padStart(2, '0')

internal fun Byte.toBoolean(): Boolean = when(this) {
    0x00.toByte() -> false
    0x01.toByte() -> true
    else -> throw MessageParseException.InvalidMessage("Expected boolean (0 or 1), got $this")
}