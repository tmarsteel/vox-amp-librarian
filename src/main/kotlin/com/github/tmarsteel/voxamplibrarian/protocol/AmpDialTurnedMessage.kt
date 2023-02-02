package com.github.tmarsteel.voxamplibrarian.protocol

data class AmpDialTurnedMessage(
    val dial: Byte,
    val value: TwoByteDial,
) : MessageToAmp, MessageToHost {
    override fun writeTo(out: BinaryOutput) {
        out.write(PREFIX)
        out.write(dial)
        out.write(value)
    }

    companion object : MidiProtocolMessage.Factory<AmpDialTurnedMessage> {
        val PREFIX = byteArrayOf(0x30, 0x00, 0x01, 0x34, 0x41, 0x04)
        override fun parse(fullMessage: BinaryInput): AmpDialTurnedMessage {
            requirePrefix(fullMessage, PREFIX, AmpDialTurnedMessage::class)

            return AmpDialTurnedMessage(
                fullMessage.nextByte(),
                TwoByteDial.readFrom(fullMessage),
            )
        }
    }
}