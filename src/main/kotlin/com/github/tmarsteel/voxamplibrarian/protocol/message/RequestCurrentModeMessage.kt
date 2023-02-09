package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.BinaryOutput

class RequestCurrentModeMessage : MessageToAmp<CurrentModeResponse> {
    override val responseFactory = CurrentModeResponse.Companion
    override fun writeTo(out: BinaryOutput) {
        out.write(byteArrayOf(
            0x30, 0x00, 0x01, 0x34, 0x12,
        ))
    }
}