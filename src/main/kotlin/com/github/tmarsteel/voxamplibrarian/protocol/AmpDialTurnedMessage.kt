package com.github.tmarsteel.voxamplibrarian.protocol

data class AmpDialTurnedMessage(
    val dial: Byte,
    val value: TwoByteDial,
) : MessageToAmp, MessageToHost {
    override fun writeTo(out: BinaryOutput) {
        val (valueFirst, valueSecond) = value.protocolBytes

        out.write(PREFIX)
        out.write(dial, valueFirst, valueSecond)
    }

    companion object : MidiProtocolMessage.Factory<AmpDialTurnedMessage> {
        val PREFIX = byteArrayOf(0x30, 0x00, 0x01, 0x34, 0x41, 0x04)
        override fun parse(fullMessage: ByteArray): AmpDialTurnedMessage {
            if (!(fullMessage startsWith PREFIX)) {
                throw MessageParseException.PrefixNotRecognized(AmpDialTurnedMessage::class)
            }

            checkMessageSize(fullMessage, PREFIX.size + 3)
            return AmpDialTurnedMessage(
                fullMessage[PREFIX.size + 0],
                TwoByteDial.fromProtocolBytes(Pair(
                    fullMessage[PREFIX.size + 1],
                    fullMessage[PREFIX.size + 2],
                ))
            )
        }
    }
}