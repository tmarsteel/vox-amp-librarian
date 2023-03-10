package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.BinaryOutput
import com.github.tmarsteel.voxamplibrarian.protocol.ProgramSlot
import com.github.tmarsteel.voxamplibrarian.protocol.message.ResponseHandler.Companion.singleMessage

data class RequestUserProgramMessage(
    val slot: ProgramSlot,
) : MessageToAmp<UserProgramResponse> {
    override fun createResponseHandler() = singleMessage(UserProgramResponse)

    override fun writeTo(out: BinaryOutput) {
        out.write(byteArrayOf(
            0x30, 0x00, 0x01, 0x34, 0x1C, 0x00
        ))
        out.write(slot)
    }
}