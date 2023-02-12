package com.github.tmarsteel.voxamplibrarian.appmodel

data class SimulationConfiguration(
    val programName: String?,
    val amplifier: DeviceConfiguration<AmplifierDescriptor>,
    val pedalOne: DeviceConfiguration<SlotOnePedalDescriptor>,
    val pedalTwo: DeviceConfiguration<SlotTwoPedalDescriptor>,
    val reverbPedal: DeviceConfiguration<ReverbPedalDescriptor>,
) {
    fun rejectInvalid() {
        amplifier.rejectInvalid()
        pedalOne.rejectInvalid()
        pedalTwo.rejectInvalid()
        reverbPedal.rejectInvalid()
    }

    companion object {
        val DEFAULT = SimulationConfiguration(
            null,
            DeviceConfiguration.defaultOf(VoxAc30Amplifier),
            DeviceConfiguration.defaultOf(CompressorPedalDescriptor),
            DeviceConfiguration.defaultOf(FlangerPedalDescriptor),
            DeviceConfiguration.defaultOf(RoomReverbPedalDescriptor),
        )
    }
}