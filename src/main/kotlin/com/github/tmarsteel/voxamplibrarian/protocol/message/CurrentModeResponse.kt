package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.*
import com.github.tmarsteel.voxamplibrarian.protocol.ProgramSlot

class CurrentModeResponse(
    val mode: Mode,
    val slot: ProgramSlot?,
    val presetIdentifier: Byte?,
) : MessageToHost {
    companion object : MidiProtocolMessage.Factory<CurrentModeResponse> {
        val PREFIX = byteArrayOf(
            0x30, 0x00, 0x01, 0x34, 0x42
        )
        override val type = CurrentModeResponse::class
        override fun parse(fullMessage: BinaryInput): CurrentModeResponse {
            requirePrefix(fullMessage, PREFIX, CurrentModeResponse::class)
            val mode = fullMessage.nextByte()
            return when (mode.toInt()) {
                0x00 -> {
                    val slot = ProgramSlot.readFrom(fullMessage)
                    requireEOF(fullMessage)
                    CurrentModeResponse(Mode.PROGRAM_SLOT, slot, null)
                }
                0x01 -> {
                    val presetIdentifier = fullMessage.nextByte()
                    requireEOF(fullMessage)
                    CurrentModeResponse(Mode.PRESET, null, presetIdentifier)
                }
                0x02 -> {
                    requireNextByteEquals(fullMessage, 0x00)
                    requireEOF(fullMessage)
                    CurrentModeResponse(Mode.MANUAL, null, null)
                }
                else -> {
                    throw MessageParseException.InvalidMessage("Unrecognized mode: ${mode.hex()}")
                }
            }
        }
    }

    enum class Mode {
        PROGRAM_SLOT,
        PRESET,
        MANUAL
    }
}