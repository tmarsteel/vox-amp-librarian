package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.protocol.BinaryInput
import com.github.tmarsteel.voxamplibrarian.protocol.BinaryOutput
import com.github.tmarsteel.voxamplibrarian.protocol.ZeroToTenDial
import com.github.tmarsteel.voxamplibrarian.protocol.requireEOF
import com.github.tmarsteel.voxamplibrarian.protocol.requireNextByteEquals
import com.github.tmarsteel.voxamplibrarian.protocol.requirePrefix

data class NoiseReductionSensitivityChangedMessage(
    val sensitivity: ZeroToTenDial,
) : CommandWithoutResponse, MessageToHost {
    override fun writeTo(out: BinaryOutput) {
        out.write(PREFIX)
        out.write(sensitivity)
        out.write(0x00)
    }

    companion object : MidiProtocolMessage.Factory<NoiseReductionSensitivityChangedMessage> {
        val PREFIX = byteArrayOf(0x30, 0x00, 0x01, 0x34, 0x41, 0x01, 0x00)
        override fun parse(fullMessage: BinaryInput): NoiseReductionSensitivityChangedMessage {
            requirePrefix(fullMessage, PREFIX, NoiseReductionSensitivityChangedMessage::class)
            val sensitivity = ZeroToTenDial.readFrom(fullMessage)
            requireNextByteEquals(fullMessage, 0x00)
            requireEOF(fullMessage)

            return NoiseReductionSensitivityChangedMessage(sensitivity)
        }
    }
}