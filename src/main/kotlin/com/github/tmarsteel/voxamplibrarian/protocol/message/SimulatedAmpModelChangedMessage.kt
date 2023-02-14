package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import com.github.tmarsteel.voxamplibrarian.BinaryOutput
import com.github.tmarsteel.voxamplibrarian.protocol.AmpModel
import com.github.tmarsteel.voxamplibrarian.requireEOF
import com.github.tmarsteel.voxamplibrarian.requireNextByteEquals
import com.github.tmarsteel.voxamplibrarian.requirePrefix

data class SimulatedAmpModelChangedMessage(
    val model: AmpModel,
) : CommandWithoutResponse, MessageToHost {
    override fun writeTo(out: BinaryOutput) {
        out.write(PREFIX)
        out.write(model)
        out.write(0x00)
    }

    companion object : MidiProtocolMessage.Factory<SimulatedAmpModelChangedMessage> {
        val PREFIX = byteArrayOf(0x30, 0x00, 0x01, 0x34, 0x41, 0x03, 0x00)
        override val type = SimulatedAmpModelChangedMessage::class
        override fun parse(fullMessage: BinaryInput): SimulatedAmpModelChangedMessage {
            requirePrefix(fullMessage, PREFIX, SimulatedAmpModelChangedMessage::class)
            val model = AmpModel.readFrom(fullMessage)
            requireNextByteEquals(fullMessage, 0x00)
            requireEOF(fullMessage)

            return SimulatedAmpModelChangedMessage(model)
        }
    }
}