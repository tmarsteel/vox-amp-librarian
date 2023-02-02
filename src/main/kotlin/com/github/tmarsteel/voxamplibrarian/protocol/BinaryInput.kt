package com.github.tmarsteel.voxamplibrarian.protocol

interface BinaryInput {
    /**
     * @throws MessageParseException.InvalidMessage
     */
    fun nextByte(): Byte

    fun nextBytes(target: ByteArray, offset: Int = 0, length: Int = target.size) {
        if (offset !in target.indices || (offset + length - 1) !in target.indices) {
            throw RuntimeException("Array index out of bounds")
        }

        if (bytesRemaining < length) {
            throw MessageParseException.InvalidMessage("Expected $length more bytes, found only $bytesRemaining")
        }

        for (i in offset until offset + length) {
            target[i] = nextByte()
        }
    }

    val bytesRemaining: Int

    fun seekToStart()

    fun skip(nBytes: Int) {
        if (bytesRemaining < nBytes) {
            throw MessageParseException.InvalidMessage("Expected $nBytes more bytes, got only $bytesRemaining")
        }

        repeat(nBytes) {
            nextByte()
        }
    }
}