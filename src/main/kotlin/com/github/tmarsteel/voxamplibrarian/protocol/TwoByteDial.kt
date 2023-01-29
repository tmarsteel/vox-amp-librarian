package com.github.tmarsteel.voxamplibrarian.protocol

data class TwoByteDial(val semanticValue: UShort) {
    init {
        require(semanticValue <= 32895u)
    }

    val protocolBytes: Pair<Byte, Byte>
        get() {
            val protocolValue = semanticValue + (semanticValue / 0x80u) * 0x80u
            check(protocolValue in 0u..UShort.MAX_VALUE.toUInt())

            return Pair(
                (protocolValue and 0xFFu).toByte(),
                ((protocolValue shr 8) and 0xFFu).toByte(),
            )
        }

    companion object {
        fun fromProtocolBytes(bytes: Pair<Byte, Byte>): TwoByteDial {
            val protocolValue = bytes.first.toLong() or (bytes.second.toLong() shl 8)
            val millihertzOrMilliseconds = protocolValue - (protocolValue / 0x100L) * 0x80
            return TwoByteDial(millihertzOrMilliseconds.toUShort())
        }
    }
}