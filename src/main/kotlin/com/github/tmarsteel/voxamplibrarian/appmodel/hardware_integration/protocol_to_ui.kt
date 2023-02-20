package com.github.tmarsteel.voxamplibrarian.appmodel.hardware_integration

import com.github.tmarsteel.voxamplibrarian.appmodel.*
import com.github.tmarsteel.voxamplibrarian.protocol.Program
import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageToHost
import com.github.tmarsteel.voxamplibrarian.protocol.message.ProgramSlotChangedMessage

internal fun Program.toUiDataModel(): SimulationConfiguration {
    return SimulationConfiguration(
        programName = programName.name,
        amplifier = DeviceConfiguration.from(this, AmplifierDescriptor.ALL),
        pedalOne = DeviceConfiguration.from(this, SlotOnePedalDescriptor.ALL),
        pedalTwo = DeviceConfiguration.from(this, SlotTwoPedalDescriptor.ALL),
        reverbPedal = DeviceConfiguration.from(this, ReverbPedalDescriptor.ALL),
    )
}

fun VtxAmpState.plus(diff: MessageToHost): VtxAmpState {
    when (diff) {
        is ProgramSlotChangedMessage -> {
            throw DifferentialUpdateNotSupportedException()
        }
        else -> {
            return this.withActiveConfiguration(this.activeConfiguration.plus(diff))
        }
    }
}