package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.BinaryOutput
import com.github.tmarsteel.voxamplibrarian.protocol.ProgramSlot

/**
 * TODO! it is currently unclear what this message does.
 * Test writing data to a currently un-selected program
 * without sending this
 */
class PersistUserProgramMessage(
    val slot: ProgramSlot,
) : CommandWithoutResponse {
    override fun writeTo(out: BinaryOutput) {
        out.write(byteArrayOf(
            0x30, 0x00, 0x01, 0x34, 0x4e, 0x00
        ))
        out.write(slot)
    }
}