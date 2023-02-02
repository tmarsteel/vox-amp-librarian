package com.github.tmarsteel.voxamplibrarian.protocol

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import com.github.tmarsteel.voxamplibrarian.ByteArrayBinaryInput

fun String.readHexStream(): BinaryInput {
    return replace(" ", "")
        .replace("\n", "")
        .replace("\t", "")
        .windowed(2, 2)
        .map { it.toString().toByte(16) }
        .toByteArray()
        .let(::ByteArrayBinaryInput)
}