package com.github.tmarsteel.voxamplibrarian.protocol

interface BinaryInput {
    /**
     * @throws MessageParseException.InvalidMessage
     */
    fun nextByte(): Byte

    val bytesRemaining: Int

    fun seekToStart()
}