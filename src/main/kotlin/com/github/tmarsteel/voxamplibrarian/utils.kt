package com.github.tmarsteel.voxamplibrarian

import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageParseException
import com.github.tmarsteel.voxamplibrarian.protocol.message.MidiProtocolMessage
import kotlin.reflect.KClass

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

internal fun requireNextByteEquals(data: BinaryInput, expected: Byte) {
    val actual = data.nextByte()
    if (actual != expected) {
        throw MessageParseException.InvalidMessage(
            "Unexpected ${actual.hex()}, expected ${expected.hex()}"
        )
    }
}

internal fun requireEOF(data: BinaryInput) {
    if (data.bytesRemaining > 0) {
        throw MessageParseException.InvalidMessage(
            "Message is too long, got ${data.bytesRemaining} extra bytes"
        )
    }
}

internal fun Byte.hex(): String = "0x" + toString(16).padStart(2, '0')

internal fun Byte.toBoolean(): Boolean = when(this) {
    0x00.toByte() -> false
    0x01.toByte() -> true
    else -> throw MessageParseException.InvalidMessage("Expected boolean (0 or 1), got $this")
}

fun BinaryInput.preview(): String {
    seekToStart()
    val nBytesTotal = bytesRemaining
    val nPreviewBytes = bytesRemaining.coerceAtMost(16)
    val previewBytes = ByteArray(nPreviewBytes)
    nextBytes(previewBytes)
    val previewBytesSting = previewBytes.joinToString(separator = " ") {
        it.toString(16).padStart(2, '0')
    }

    return if (nPreviewBytes < nBytesTotal) {
        "$previewBytesSting ... (${nBytesTotal - nPreviewBytes} more bytes)"
    } else {
        previewBytesSting
    }
}