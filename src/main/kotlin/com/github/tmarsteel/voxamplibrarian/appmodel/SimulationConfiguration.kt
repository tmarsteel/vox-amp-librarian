package com.github.tmarsteel.voxamplibrarian.appmodel

data class SimulationConfiguration(
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
}