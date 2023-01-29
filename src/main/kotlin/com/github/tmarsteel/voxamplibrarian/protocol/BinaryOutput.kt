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
}