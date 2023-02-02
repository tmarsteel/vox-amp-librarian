package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.protocol.BinaryOutput
import com.github.tmarsteel.voxamplibrarian.protocol.PedalSlot
import com.github.tmarsteel.voxamplibrarian.protocol.PedalType
import com.github.tmarsteel.voxamplibrarian.protocol.ReverbPedalType
import com.github.tmarsteel.voxamplibrarian.protocol.Slot1PedalType
import com.github.tmarsteel.voxamplibrarian.protocol.Slot2PedalType

class SetEffectPedalTypeMessage(
    val type: PedalType,
) : MessageToAmp<EffectDialTurnedMessage> {
    override val responseFactory = EffectDialTurnedMessage

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