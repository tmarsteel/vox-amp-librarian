package com.github.tmarsteel.voxamplibrarian.protocol

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import com.github.tmarsteel.voxamplibrarian.BinaryOutput
import com.github.tmarsteel.voxamplibrarian.hex
import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageParseException

enum class TubeBias(override val protocolValue: Byte) : SingleByteProtocolSerializable {
    OFF(0x00),
    COLD(0x01),
    HOT(0x02),
    ;

    override fun writeTo(out: BinaryOutput) {
        out.write(protocolValue)
    }

    companion object {
        fun readFrom(input: BinaryInput): TubeBias {
            val value = input.nextByte()
            return enumValues<TubeBias>().find { it.protocolValue == value }
                ?: throw MessageParseException.InvalidMessage(
                    "Unknown tube bias ${value.hex()}"
                )
        }
    }
}