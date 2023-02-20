package com.github.tmarsteel.voxamplibrarian

import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageParseException

interface BinaryInput {
    /**
     * @throws MessageParseException.InvalidMessage
     */
    fun nextByte(): Byte

    /**
     * reads two bytes and interprets them as little endian
     */
    fun nextUShort(): UShort {
        val buffer = ByteArray(2)
        nextBytes(buffer)
        return ((buffer[1].toInt() shl 8) or buffer[0].toInt()).toUShort()
    }

    fun nextBytes(target: ByteArray, offset: Int = 0, length: Int = target.size) {
        if (offset !in target.indices) {
            throw RuntimeException("Array index out of bounds: the starting offset $offset is not in the bounds of the target (total size ${target.size})")
        }

        if ((offset + length - 1) !in target.indices) {
            throw RuntimeException("Array index out of bounds: the target cannot hold $length bytes from offset $offset on (total size only ${target.size})")
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