package com.github.tmarsteel.voxamplibrarian.protocol

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