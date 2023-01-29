package com.github.tmarsteel.voxamplibrarian.protocol

fun String.readHexStream(): BinaryInput {
    return split(' ')
        .map { it.toByte(16) }
        .toByteArray()
        .let(::ByteArrayBinaryInput)
}