package com.github.tmarsteel.voxamplibrarian.protocol

data class RequestUserProgramMessage(
    val slot: ProgramSlot,
) : HostInitiatedExchange<UserProgramResponse> {
    override val responseFactory = UserProgramResponse.Companion

    override fun writeTo(out: BinaryOutput) {
        out.write(byteArrayOf(
            0x30, 0x00, 0x01, 0x34, 0x1C, 0x00
        ))
        out.write(slot)
    }
}