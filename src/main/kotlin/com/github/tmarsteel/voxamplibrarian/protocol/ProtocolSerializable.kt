package com.github.tmarsteel.voxamplibrarian.protocol

interface ProtocolSerializable {
    fun writeTo(out: BinaryOutput)
}