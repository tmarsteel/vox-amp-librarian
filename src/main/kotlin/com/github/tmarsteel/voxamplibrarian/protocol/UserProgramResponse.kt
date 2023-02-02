package com.github.tmarsteel.voxamplibrarian.protocol

data class UserProgramResponse(
    val slot: ProgramSlot,
    val program: Program,
) : MessageToHost {
    companion object : MidiProtocolMessage.Factory<UserProgramResponse> {
        val PREFIX = byteArrayOf(
            0x30, 0x00, 0x01, 0x34, 0x4c, 0x00
        )

        override fun parse(fullMessage: BinaryInput): UserProgramResponse {
            requirePrefix(fullMessage, PREFIX, UserProgramResponse::class)
            val slot = ProgramSlot.readFrom(fullMessage)
            requireNextByteEquals(fullMessage, 0x00)
            val program = Program.readFrom(fullMessage)
            requireEOF(fullMessage)

            return UserProgramResponse(slot, program)
        }
    }
}