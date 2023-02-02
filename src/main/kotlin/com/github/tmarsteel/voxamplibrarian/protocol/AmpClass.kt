package com.github.tmarsteel.voxamplibrarian.protocol

enum class AmpClass(val protocolValue: Byte) : ProtocolSerializable {
    A(0x00),
    AB(0x01),
    ;

    override fun writeTo(out: BinaryOutput) {
        out.write(protocolValue)
    }

    companion object {
        fun readFrom(input: BinaryInput): AmpClass {
            val value = input.nextByte()
            return enumValues<AmpClass>().find { it.protocolValue == value }
                ?: throw MessageParseException.InvalidMessage(
                    "Unknown amp class ${value.hex()}"
                )
        }
    }
}