package com.github.tmarsteel.voxamplibrarian.protocol

internal infix fun ByteArray.startsWith(prefix: ByteArray): Boolean {
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

internal fun checkMessageSize(message: ByteArray, expectedSize: Int) {
    if (message.size != expectedSize) {
        throw MessageParseException.InvalidMessageException("The message must be exactly $expectedSize bytes long, but got ${message.size} bytes.")
    }
}