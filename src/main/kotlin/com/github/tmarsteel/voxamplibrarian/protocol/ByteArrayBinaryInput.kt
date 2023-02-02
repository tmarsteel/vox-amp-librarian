package com.github.tmarsteel.voxamplibrarian.protocol

import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageParseException

class ByteArrayBinaryInput(private val data: ByteArray) : BinaryInput {
    private var offset: Int = 0

    override fun nextByte(): Byte {
        if (offset >= data.size) {
            throw MessageParseException.InvalidMessage("Message is too small, wanted to read more bytes but none available.")
        }

        return data[offset++]
    }

    override val bytesRemaining: Int
        get() = data.size - offset

    override fun seekToStart() {
        offset = 0
    }
}