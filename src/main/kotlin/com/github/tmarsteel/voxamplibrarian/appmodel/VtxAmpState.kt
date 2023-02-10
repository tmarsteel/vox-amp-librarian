package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.protocol.ProgramSlot

sealed class VtxAmpState {
    abstract val configuration: SimulationConfiguration
    abstract fun withConfiguration(newConfig: SimulationConfiguration): VtxAmpState

    class ProgramSlotSelected(val slot: ProgramSlot, override val configuration: SimulationConfiguration) : VtxAmpState() {
        override fun withConfiguration(newConfig: SimulationConfiguration): VtxAmpState {
            return ProgramSlotSelected(slot, newConfig)
        }
    }

    class ManualMode(override val configuration: SimulationConfiguration) : VtxAmpState() {
        override fun withConfiguration(newConfig: SimulationConfiguration): VtxAmpState {
            return ManualMode(newConfig)
        }
    }

    class PresetMode(val identifier: Byte, override val configuration: SimulationConfiguration) : VtxAmpState() {
        override fun withConfiguration(newConfig: SimulationConfiguration): VtxAmpState {
            return PresetMode(identifier, newConfig)
        }
    }
}