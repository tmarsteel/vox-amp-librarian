package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.BinaryOutput
import com.github.tmarsteel.voxamplibrarian.protocol.Program
import com.github.tmarsteel.voxamplibrarian.protocol.ProgramSlot

data class WriteUserProgramMessage(
    val slot: ProgramSlot,
    val program: Program,
) : CommandWithoutResponse {
    override fun writeTo(out: BinaryOutput) {
        out.write(byteArrayOf(
            0x30, 0x00, 0x01, 0x34, 0x4c, 0x00
        ))
        out.write(slot)
        out.write(0x00)
        out.write(program)
    }
}