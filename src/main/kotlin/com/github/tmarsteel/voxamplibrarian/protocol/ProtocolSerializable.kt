package com.github.tmarsteel.voxamplibrarian.protocol

import com.github.tmarsteel.voxamplibrarian.BinaryOutput

interface ProtocolSerializable {
    fun writeTo(out: BinaryOutput)
}

interface SingleByteProtocolSerializable : ProtocolSerializable {
    val protocolValue: Byte
    override fun writeTo(out: BinaryOutput) = out.write(protocolValue)
}