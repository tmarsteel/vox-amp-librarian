package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.protocol.ProgramSlot

sealed class VtxAmpState {
    abstract val storedUserPrograms: Map<ProgramSlot, SimulationConfiguration>
    abstract val activeConfiguration: SimulationConfiguration
    abstract fun withActiveConfiguration(newConfig: SimulationConfiguration): VtxAmpState
    abstract fun withStoredUserProgram(slot: ProgramSlot, configuration: SimulationConfiguration): VtxAmpState

    data class ProgramSlotSelected(
        override val storedUserPrograms: Map<ProgramSlot, SimulationConfiguration>,
        override val activeConfiguration: SimulationConfiguration,
        val slot: ProgramSlot
    ) : VtxAmpState() {
        override fun withActiveConfiguration(newConfig: SimulationConfiguration) = copy(
            activeConfiguration = newConfig,
        )

        override fun withStoredUserProgram(slot: ProgramSlot, configuration: SimulationConfiguration) = copy(
            storedUserPrograms = storedUserPrograms + (slot to configuration),
        )
    }

    data class ManualMode(
        override val storedUserPrograms: Map<ProgramSlot, SimulationConfiguration>,
        override val activeConfiguration: SimulationConfiguration,
    ) : VtxAmpState() {
        override fun withActiveConfiguration(newConfig: SimulationConfiguration) = copy(
            activeConfiguration = newConfig
        )

        override fun withStoredUserProgram(slot: ProgramSlot, configuration: SimulationConfiguration) = copy(
            storedUserPrograms = storedUserPrograms + (slot to configuration),
        )
    }

    data class PresetMode(
        override val storedUserPrograms: Map<ProgramSlot, SimulationConfiguration>,
        override val activeConfiguration: SimulationConfiguration,
        val identifier: Byte,
    ) : VtxAmpState() {
        override fun withActiveConfiguration(newConfig: SimulationConfiguration) = copy(
            activeConfiguration = newConfig
        )

        override fun withStoredUserProgram(slot: ProgramSlot, configuration: SimulationConfiguration) = copy(
            storedUserPrograms = storedUserPrograms + (slot to configuration),
        )
    }

    companion object {
        val DEFAULT = ManualMode(ProgramSlot.values().associateWith { SimulationConfiguration.DEFAULT }, SimulationConfiguration.DEFAULT)
    }
}