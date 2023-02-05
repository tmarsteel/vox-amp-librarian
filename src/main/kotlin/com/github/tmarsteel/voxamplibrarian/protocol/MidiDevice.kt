package com.github.tmarsteel.voxamplibrarian.protocol

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import com.github.tmarsteel.voxamplibrarian.BinaryOutput

interface MidiDevice {
    suspend fun sendSysExMessage(manufacturerId: Byte, writer: (BinaryOutput) -> Unit)
    var incomingSysExMessageHandler: (manufacturerId: Byte, payload: BinaryInput) -> Unit
}