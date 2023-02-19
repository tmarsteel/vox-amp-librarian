package com.github.tmarsteel.voxamplibrarian.protocol

import com.github.tmarsteel.voxamplibrarian.*
import kotlinx.browser.window

data class TwoByteDial(val semanticValue: UShort) : ProtocolSerializable {
    init {
        require(semanticValue <= 32895u)
    }

    /**
     * Sometimes the protocol reserves two bytes for a value, but
     * single-byte data is transmitted in that place. Use this method
     * to convert.
     * @see ZeroToTenDial.asTwoByte
     */
    fun asZeroToTen(): ZeroToTenDial {
        check(semanticValue in 0u..127u)
        return ZeroToTenDial(semanticValue.toByte())
    }

    override fun writeTo(out: BinaryOutput) {
        val protocolValue = semanticValue + (semanticValue / 0x80u) * 0x80u
        check(protocolValue in 0u..UShort.MAX_VALUE.toUInt())

        out.write((protocolValue and 0xFFu).toByte())
        out.write(((protocolValue shr 8) and 0xFFu).toByte())
    }

    companion object {
        fun readFrom(data: BinaryInput): TwoByteDial {
            val protocolValue = data.nextByte().toLong() or (data.nextByte().toLong() shl 8)
            val millihertzOrMilliseconds = protocolValue - (protocolValue / 0x100L) * 0x80
            return TwoByteDial(millihertzOrMilliseconds.toUShort())
        }

        init {
            window.asDynamic().bullshitEncoder = bse@ { semantic: Int ->
                val out = BufferedBinaryOutput()
                TwoByteDial(semantic.toUShort()).writeTo(out)
                val input = out.copyToInput()
                return@bse input.nextByte().hex() + " " + input.nextByte().hex()
            }
            window.asDynamic().bullshitDecoder = bsd@ { protocol: String ->
                val input = ByteArrayBinaryInput(protocol.parseHexStream())
                return@bsd readFrom(input).semanticValue.toInt()
            }
        }
    }
}