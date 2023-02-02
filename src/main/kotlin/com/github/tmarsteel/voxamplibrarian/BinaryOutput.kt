package com.github.tmarsteel.voxamplibrarian

import com.github.tmarsteel.voxamplibrarian.protocol.ProtocolSerializable

interface BinaryOutput {
    fun write(byte: Byte)

    fun write(bytes: ByteArray, offset: Int = 0, length: Int = bytes.size) {
        for (i in offset until (offset + length)) {
            write(bytes[i])
        }
    }

    fun write(vararg bytes: Byte) {
        write(bytes)
    }

    fun write(bytes: Pair<Byte, Byte>) {
        write(bytes.first)
        write(bytes.second)
    }

    fun write(value: ProtocolSerializable) {
        value.writeTo(this)
    }

    fun write(value: Boolean) {
        write(if (value) 0x01 else 0x00)
    }
}