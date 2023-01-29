package com.github.tmarsteel.voxamplibrarian.protocol

interface BinaryOutput {
    fun write(byte: Byte)
    fun write(bytes: ByteArray) {
        for (byte in bytes) {
            write(byte)
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
}