package com.github.tmarsteel.voxamplibrarian.protocol

class SetEffectPedalTypeMessage(
    val type: PedalType,
) : MessageToAmp<EffectDialTurnedMessage> {
    override val responseFactory = EffectDialTurnedMessage.Companion

    override fun writeTo(out: BinaryOutput) {
        out.write(byteArrayOf(
            0x30, 0x00, 0x01, 0x34, 0x41, 0x03
        ))

        when (type) {
            is Slot1PedalType -> {
                out.write(PedalSlot.PEDAL1)
                out.write(type.protocolValue)
            }
            is Slot2PedalType -> {
                out.write(PedalSlot.PEDAL2)
                out.write(type.protocolValue)
            }
            is ReverbPedalType -> {
                out.write(PedalSlot.REVERB)
                out.write(type.protocolValue)
            }
        }
    }
}