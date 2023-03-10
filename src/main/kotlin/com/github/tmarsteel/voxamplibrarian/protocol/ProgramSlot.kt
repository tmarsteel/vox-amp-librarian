package com.github.tmarsteel.voxamplibrarian.protocol

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import com.github.tmarsteel.voxamplibrarian.BinaryOutput
import com.github.tmarsteel.voxamplibrarian.hex
import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageParseException

enum class ProgramSlot(val protocolValue: Byte) : ProtocolSerializable {
    A1(0x00),
    A2(0x01),
    A3(0x02),
    A4(0x03),
    B1(0x04),
    B2(0x05),
    B3(0x06),
    B4(0x07),
    ;

    override fun writeTo(out: BinaryOutput) {
        out.write(protocolValue)
    }

    companion object {
        fun readFrom(input: BinaryInput): ProgramSlot {
            val value = input.nextByte()
            return enumValues<ProgramSlot>().find { it.protocolValue == value }
                ?: throw MessageParseException.InvalidMessage(
                    "Unknown program slot ${value.hex()}"
                )
        }
    }
}