package com.github.tmarsteel.voxamplibrarian.protocol

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import com.github.tmarsteel.voxamplibrarian.BinaryOutput
import com.github.tmarsteel.voxamplibrarian.hex
import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageParseException

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