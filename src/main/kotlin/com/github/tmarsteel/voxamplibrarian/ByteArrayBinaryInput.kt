package com.github.tmarsteel.voxamplibrarian

import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageParseException

class ByteArrayBinaryInput(
    private val data: ByteArray,

    /**
     * limits the view into [data], starts at [beginIndex] inclusive
     */
    private val beginIndex: Int = 0,

    /**
     * limits the view into [data], ends at [endIndex] inclusive
     */
    private val endIndex: Int = data.lastIndex
) : BinaryInput {
    init {
        require(beginIndex in data.indices)
        require(endIndex in data.indices)
    }
    override var position: Int = beginIndex
        private set

    override fun nextByte(): Byte {
        if (position > endIndex) {
            throw MessageParseException.InvalidMessage("Message is too small, wanted to read more bytes but none available.")
        }

        return data[position++]
    }

    override val bytesRemaining: Int
        get() = endIndex - position + 1

    override fun seekToStart() {
        position = beginIndex
    }
}