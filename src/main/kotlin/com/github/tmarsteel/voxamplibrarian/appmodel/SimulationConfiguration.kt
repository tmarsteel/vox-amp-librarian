package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.protocol.AmpModel
import com.github.tmarsteel.voxamplibrarian.protocol.ReverbPedalType
import com.github.tmarsteel.voxamplibrarian.protocol.Slot1PedalType
import com.github.tmarsteel.voxamplibrarian.protocol.Slot2PedalType
import com.github.tmarsteel.voxamplibrarian.protocol.message.EffectPedalTypeChangedMessage
import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageToHost
import com.github.tmarsteel.voxamplibrarian.protocol.message.SimulatedAmpModelChangedMessage

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

    /**
     * @return this configuration after it has been modified with the given event, or `null` if not affected.
     */
    fun plus(diff: MessageToHost): SimulationConfiguration {
        when (diff) {
            is EffectPedalTypeChangedMessage -> return when (diff.type) {
                is Slot1PedalType -> copy(pedalOne = pedalOne.withDescriptor(diff.type.descriptor))
                is Slot2PedalType -> copy(pedalTwo = pedalTwo.withDescriptor(diff.type.descriptor))
                is ReverbPedalType -> copy(reverbPedal = reverbPedal.withDescriptor(diff.type.descriptor))
            }
            is SimulatedAmpModelChangedMessage -> return copy(amplifier = amplifier.withDescriptor(diff.model.descriptor))
            else -> {
                val newAmp = amplifier.tryApplyEvent(diff)
                val newPedalOne = pedalOne.tryApplyEvent(diff)
                val newPedalTwo = pedalTwo.tryApplyEvent(diff)
                val newReverbPedal = reverbPedal.tryApplyEvent(diff)

                val affectedDevices = listOfNotNull(newAmp, newPedalOne, newPedalTwo, newReverbPedal)
                if (affectedDevices.isEmpty()) {
                    console.error("Unimplemented message ${diff::class.simpleName}", diff)
                    return this
                }

                if (affectedDevices.size > 1) {
                    console.error("Multiple devices affected by ${diff::class.simpleName}: ${affectedDevices.joinToString(transform = { it.descriptor.name })}", diff)
                }

                return copy(
                    amplifier = newAmp ?: amplifier,
                    pedalOne = newPedalOne ?: pedalOne,
                    pedalTwo = newPedalTwo ?: pedalTwo,
                    reverbPedal = newReverbPedal ?: reverbPedal,
                )
            }
        }
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

private val Slot1PedalType.descriptor: SlotOnePedalDescriptor
    get() = SlotOnePedalDescriptor.ALL.single { it.pedalType == this }

private val Slot2PedalType.descriptor: SlotTwoPedalDescriptor
    get() = SlotTwoPedalDescriptor.ALL.single { it.pedalType == this }

private val ReverbPedalType.descriptor: ReverbPedalDescriptor
    get() = ReverbPedalDescriptor.ALL.single { it.pedalType == this }

private val AmpModel.descriptor: AmplifierDescriptor
    get() = AmplifierDescriptor.ALL.single { it.protocolModel == this }