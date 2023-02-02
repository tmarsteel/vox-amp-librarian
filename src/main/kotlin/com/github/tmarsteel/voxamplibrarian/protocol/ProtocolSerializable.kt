package com.github.tmarsteel.voxamplibrarian.protocol

import com.github.tmarsteel.voxamplibrarian.BinaryOutput

interface ProtocolSerializable {
    fun writeTo(out: BinaryOutput)
}