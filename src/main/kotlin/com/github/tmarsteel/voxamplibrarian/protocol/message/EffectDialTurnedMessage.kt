package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import com.github.tmarsteel.voxamplibrarian.BinaryOutput
import com.github.tmarsteel.voxamplibrarian.protocol.PedalSlot
import com.github.tmarsteel.voxamplibrarian.protocol.TwoByteDial
import com.github.tmarsteel.voxamplibrarian.requirePrefix

data class EffectDialTurnedMessage(
    val pedalSlot: PedalSlot,
    val dialIndex: Byte,
    val value: TwoByteDial,
) : CommandWithoutResponse, MessageToHost {
    override fun writeTo(out: BinaryOutput) {
        out.write(PREFIX)
        out.write(pedalSlot)
        out.write(dialIndex)
        out.write(value)
    }

    companion object : MidiProtocolMessage.Factory<EffectDialTurnedMessage> {
        val PREFIX = byteArrayOf(0x30, 0x00, 0x01, 0x34, 0x41)

        override val type = EffectDialTurnedMessage::class
        override fun parse(fullMessage: BinaryInput): EffectDialTurnedMessage {
            requirePrefix(fullMessage, PREFIX, EffectDialTurnedMessage::class)
            val slot = try {
                PedalSlot.readFrom(fullMessage)
            }
            catch (ex: MessageParseException.InvalidMessage) {
                // other settings have the same prefix (probably means different dial/parameter category)
                // try those
                throw MessageParseException.PrefixNotRecognized(EffectDialTurnedMessage::class)
            }
            return EffectDialTurnedMessage(
                slot,
                fullMessage.nextByte(),
                TwoByteDial.readFrom(fullMessage),
            )
        }
    }
}