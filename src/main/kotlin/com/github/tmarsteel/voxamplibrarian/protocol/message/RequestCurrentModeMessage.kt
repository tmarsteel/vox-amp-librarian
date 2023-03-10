package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.BinaryOutput
import com.github.tmarsteel.voxamplibrarian.protocol.message.ResponseHandler.Companion.singleMessage

class RequestCurrentModeMessage : MessageToAmp<CurrentModeResponse> {
    override fun createResponseHandler() = singleMessage(CurrentModeResponse)
    override fun writeTo(out: BinaryOutput) {
        out.write(byteArrayOf(
            0x30, 0x00, 0x01, 0x34, 0x12,
        ))
    }
}