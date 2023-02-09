package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.protocol.ProgramSlot

sealed class VtxAmpState {
    abstract val configuration: SimulationConfiguration

    class ProgramSlotSelected(val slot: ProgramSlot, override val configuration: SimulationConfiguration) : VtxAmpState()
    class ManualMode(override val configuration: SimulationConfiguration) : VtxAmpState()
    class PresetMode(val identifier: Byte, override val configuration: SimulationConfiguration) : VtxAmpState()
}