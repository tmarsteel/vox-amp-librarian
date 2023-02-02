package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import com.github.tmarsteel.voxamplibrarian.protocol.Program
import com.github.tmarsteel.voxamplibrarian.requireEOF
import com.github.tmarsteel.voxamplibrarian.requirePrefix

data class CurrentProgramResponse(
    val program: Program
) : MessageToHost {
    companion object : MidiProtocolMessage.Factory<CurrentProgramResponse> {
        val PREFIX = byteArrayOf(0x30, 0x00, 0x01, 0x34, 0x10)

        override fun parse(fullMessage: BinaryInput): CurrentProgramResponse {
            requirePrefix(fullMessage, PREFIX, CurrentProgramResponse::class)
            val program = Program.readFrom(fullMessage)
            requireEOF(fullMessage)

            return CurrentProgramResponse(program)
        }
    }
}