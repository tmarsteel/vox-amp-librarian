package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.protocol.BinaryInput
import com.github.tmarsteel.voxamplibrarian.protocol.ProgramSlot
import com.github.tmarsteel.voxamplibrarian.protocol.requireEOF
import com.github.tmarsteel.voxamplibrarian.protocol.requirePrefix

class CurrentlySelectedProgramSlotResponse(
    val slot: ProgramSlot,
) : MessageToHost {
    companion object : MidiProtocolMessage.Factory<CurrentlySelectedProgramSlotResponse> {
        val PREFIX = byteArrayOf(
            0x30, 0x00, 0x01, 0x34, 0x42, 0x00
        )
        override fun parse(fullMessage: BinaryInput): CurrentlySelectedProgramSlotResponse {
            requirePrefix(fullMessage, PREFIX, CurrentlySelectedProgramSlotResponse::class)
            val slot = ProgramSlot.readFrom(fullMessage)
            requireEOF(fullMessage)

            return CurrentlySelectedProgramSlotResponse(slot)
        }
    }
}