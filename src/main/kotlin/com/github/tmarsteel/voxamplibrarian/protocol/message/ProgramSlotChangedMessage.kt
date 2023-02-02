package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.protocol.BinaryInput
import com.github.tmarsteel.voxamplibrarian.protocol.BinaryOutput
import com.github.tmarsteel.voxamplibrarian.protocol.ProgramSlot
import com.github.tmarsteel.voxamplibrarian.protocol.requirePrefix

data class ProgramSlotChangedMessage(
    val slot: ProgramSlot,
) : CommandWithoutResponse, MessageToHost {
    override fun writeTo(out: BinaryOutput) {
        out.write(PREFIX)
        out.write(slot)
    }

    companion object : MidiProtocolMessage.Factory<ProgramSlotChangedMessage> {
        val PREFIX = byteArrayOf(0x30, 0x00, 0x01, 0x34, 0x4e, 0x00)
        override fun parse(fullMessage: BinaryInput): ProgramSlotChangedMessage {
            requirePrefix(fullMessage, PREFIX, ProgramSlotChangedMessage::class)
            return ProgramSlotChangedMessage(ProgramSlot.readFrom(fullMessage))
        }
    }
}