package com.github.tmarsteel.voxamplibrarian.protocol

import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageParseException

enum class AmpModel(val protocolValue: Byte) : ProtocolSerializable {
    DELUXE_CL_VIBRATO(0x00),
    DELUXE_CL_NORMAL(0x01),
    TWEED_410_BRIGHT(0x02),
    TWEED_410_NORMAL(0x03),
    BOUTIQUE_CL(0x04),
    BOUTIQUE_OD(0x05),
    VOX_AC30(0x06),
    VOX_AC30TB(0x07),
    BRIT_1959_TREBLE(0x08),
    BRIT_1959_NORMAL(0x09),
    BRIT_800(0x0A),
    BRIT_VM(0x0B),
    SL_OD(0x0C),
    DOUBLE_REC(0X0D),
    CALI_ELATION(0x0E),
    ERUPT_III_CH2(0x0F),
    ERUPT_III_CH3(0x10),
    BOUTIQUE_METAL(0x11),
    BRIT_OR_MKII(0x12),
    ORIGINAL_CL(0x13),
    ;

    override fun writeTo(out: BinaryOutput) {
        out.write(protocolValue)
    }

    companion object {
        fun readFrom(input: BinaryInput): AmpModel {
            val value = input.nextByte()
            return enumValues<AmpModel>().find { it.protocolValue == value }
                ?: throw MessageParseException.InvalidMessage(
                    "Unknown amp model ${value.hex()}"
                )
        }
    }
}