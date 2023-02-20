package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.protocol.ProgramSlot

sealed class VtxAmpState {
    abstract val storedUserPrograms: Map<ProgramSlot, SimulationConfiguration>
    abstract val activeConfiguration: SimulationConfiguration
    abstract fun withActiveConfiguration(newConfig: SimulationConfiguration): VtxAmpState

    init {
        //check(ProgramSlot.values().all { it in storedUserPrograms })
    }

    class ProgramSlotSelected(
        override val storedUserPrograms: Map<ProgramSlot, SimulationConfiguration>,
        override val activeConfiguration: SimulationConfiguration,
        val slot: ProgramSlot
    ) : VtxAmpState() {
        override fun withActiveConfiguration(newConfig: SimulationConfiguration): VtxAmpState {
            return ProgramSlotSelected(
                storedUserPrograms,
                newConfig,
                slot
            )
        }
    }

    class ManualMode(
        override val storedUserPrograms: Map<ProgramSlot, SimulationConfiguration>,
        override val activeConfiguration: SimulationConfiguration,
    ) : VtxAmpState() {
        override fun withActiveConfiguration(newConfig: SimulationConfiguration): VtxAmpState {
            return ManualMode(storedUserPrograms, newConfig)
        }
    }

    class PresetMode(
        override val storedUserPrograms: Map<ProgramSlot, SimulationConfiguration>,
        override val activeConfiguration: SimulationConfiguration,
        val identifier: Byte,
    ) : VtxAmpState() {
        override fun withActiveConfiguration(newConfig: SimulationConfiguration): VtxAmpState {
            return PresetMode(storedUserPrograms, activeConfiguration, identifier)
        }
    }

    companion object {
        val DEFAULT = ManualMode(ProgramSlot.values().associateWith { SimulationConfiguration.DEFAULT }, SimulationConfiguration.DEFAULT)
    }
}