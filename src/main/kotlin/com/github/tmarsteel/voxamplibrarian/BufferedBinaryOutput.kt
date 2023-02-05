package com.github.tmarsteel.voxamplibrarian

class BufferedBinaryOutput(
    initialCapacity: Int = 0xFF,
    val growthFactor: Int = 2,
) : BinaryOutput {
    init {
        require(initialCapacity >= 0)
        require(growthFactor > 1)
    }
    private var buffer = ByteArray(initialCapacity.coerceAtLeast(1))
    private var writePosition = 0

    override fun write(byte: Byte) {
        if (remaining <= 0) {
            buffer = buffer.copyOf(buffer.size * growthFactor)
        }

        buffer[writePosition++] = byte
    }

    override fun write(bytes: ByteArray, offset: Int, length: Int) {
        if (remaining < length) {
            buffer = buffer.copyOf((buffer.size * growthFactor).coerceAtLeast(writePosition + length))
        }
        bytes.copyInto(buffer, writePosition, offset, offset + length - 1)
        writePosition += length
    }

    fun copyToInput(): BinaryInput {
        return ByteArrayBinaryInput(buffer.copyOfRange(0, writePosition))
    }

    private val remaining: Int = buffer.size - writePosition
}