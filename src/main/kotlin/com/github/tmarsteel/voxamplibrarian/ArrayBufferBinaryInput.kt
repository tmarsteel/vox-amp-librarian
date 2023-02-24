package com.github.tmarsteel.voxamplibrarian

import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageParseException
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array

class ArrayBufferBinaryInput(buffer: ArrayBuffer) : BinaryInput {
    private val uint8Array = Uint8Array(buffer)
    private var position: Int = 0

    override fun nextByte(): Byte {
        if (position >= uint8Array.length) {
            throw MessageParseException.InvalidMessage("Message is too small, wanted to read more bytes but none available.")
        }

        return uint8Array[position++].toByte()
    }

    override val bytesRemaining: Int
        get() = uint8Array.length - position

    override fun seekToStart() {
        position = 0
    }
}