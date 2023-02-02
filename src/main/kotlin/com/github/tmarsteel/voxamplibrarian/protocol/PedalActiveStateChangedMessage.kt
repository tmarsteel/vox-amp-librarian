package com.github.tmarsteel.voxamplibrarian.protocol

class PedalActiveStateChangedMessage(
    val pedalSlot: PedalSlot,
    val enabled: Boolean,
) : MidiProtocolMessage {
    override fun writeTo(out: BinaryOutput) {
        out.write(PREFIX)
        out.write(pedalSlot)
        out.write(enabled)
    }

    companion object : MidiProtocolMessage.Factory<PedalActiveStateChangedMessage> {
        val PREFIX = byteArrayOf(0x30, 0x00, 0x01, 0x34, 0x41, 0x02)

        override fun parse(fullMessage: BinaryInput): PedalActiveStateChangedMessage {
            requirePrefix(fullMessage, PREFIX, PedalActiveStateChangedMessage::class)
            val slot = PedalSlot.readFrom(fullMessage)
            val enabled = fullMessage.nextByte().toBoolean()
            requireEOF(fullMessage)

            return PedalActiveStateChangedMessage(slot, enabled)
        }
    }
}