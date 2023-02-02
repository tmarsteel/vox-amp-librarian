package com.github.tmarsteel.voxamplibrarian.protocol

enum class TubeBias(val protocolValue: Byte) : ProtocolSerializable {
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