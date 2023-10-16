package com.github.tmarsteel.voxamplibrarian.appmodel.hardware_integration

import com.github.tmarsteel.voxamplibrarian.appmodel.AmplifierDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceConfiguration
import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceParameter
import com.github.tmarsteel.voxamplibrarian.appmodel.ReverbPedalDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.SimulationConfiguration
import com.github.tmarsteel.voxamplibrarian.appmodel.SlotOnePedalDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.SlotTwoPedalDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.VtxAmpState
import com.github.tmarsteel.voxamplibrarian.logging.LoggerFactory
import com.github.tmarsteel.voxamplibrarian.protocol.AmpClass
import com.github.tmarsteel.voxamplibrarian.protocol.AmpModel
import com.github.tmarsteel.voxamplibrarian.protocol.Program
import com.github.tmarsteel.voxamplibrarian.protocol.ProgramImpl
import com.github.tmarsteel.voxamplibrarian.protocol.ProgramName
import com.github.tmarsteel.voxamplibrarian.protocol.ReverbPedalType
import com.github.tmarsteel.voxamplibrarian.protocol.Slot1PedalType
import com.github.tmarsteel.voxamplibrarian.protocol.Slot2PedalType
import com.github.tmarsteel.voxamplibrarian.protocol.TubeBias
import com.github.tmarsteel.voxamplibrarian.protocol.TwoByteDial
import com.github.tmarsteel.voxamplibrarian.protocol.VoxVtxAmplifierClient
import com.github.tmarsteel.voxamplibrarian.protocol.ZeroToTenDial
import com.github.tmarsteel.voxamplibrarian.protocol.message.EffectPedalTypeChangedMessage
import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageToAmp
import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageToHost
import com.github.tmarsteel.voxamplibrarian.protocol.message.ProgramSlotChangedMessage
import com.github.tmarsteel.voxamplibrarian.protocol.message.WriteUserProgramMessage

private val logger = LoggerFactory["host-to-amp-diff"]

internal fun SimulationConfiguration.toProtocolDataModel(): Program {
    val program = ProgramImpl(
        ProgramName(programName ?: ""),
        ZeroToTenDial(0),
        AmpModel.ORIGINAL_CL,
        ZeroToTenDial(0),
        ZeroToTenDial(0),
        ZeroToTenDial(0),
        ZeroToTenDial(0),
        ZeroToTenDial(0),
        ZeroToTenDial(0),
        ZeroToTenDial(0),
        false,
        false,
        false,
        TubeBias.OFF,
        AmpClass.AB,
        // defaults
        pedalOne.getValue(DeviceParameter.Id.PedalEnabled),
        Slot1PedalType.COMP,
        TwoByteDial(0u),
        0,
        0,
        0,
        0,
        0,
        pedalTwo.getValue(DeviceParameter.Id.PedalEnabled),
        Slot2PedalType.FLANGER,
        TwoByteDial(0u),
        0,
        0,
        0,
        0,
        0,
        reverbPedal.getValue(DeviceParameter.Id.PedalEnabled),
        ReverbPedalType.ROOM,
        ZeroToTenDial(0),
        ZeroToTenDial(0),
        0,
        ZeroToTenDial(0),
        ZeroToTenDial(0),
    )

    amplifier.applyToProgram(program)
    pedalOne.applyToProgram(program)
    pedalTwo.applyToProgram(program)
    reverbPedal.applyToProgram(program)

    return program
}

sealed class ConfigurationDiff {
    /**
     * Applies this diff to the given amp.
     * @return updates to incorporate into the base state before applying more differential updates
     */
    abstract suspend fun applyTo(ampClient: VoxVtxAmplifierClient): List<MessageToHost>

    fun applyTo(ampState: VtxAmpState): VtxAmpState = ampState.withActiveConfiguration(applyTo(ampState.activeConfiguration))
    abstract fun applyTo(config: SimulationConfiguration): SimulationConfiguration

    class Parameter<V : Any>(
        val device: DeviceDescriptor<*>,
        val parameterId: DeviceParameter.Id<V>,
        val newValue: V,
    ): ConfigurationDiff() {
        override suspend fun applyTo(client: VoxVtxAmplifierClient): List<MessageToHost> {
            client.exchange(
                device.getParameter(parameterId).buildUpdateMessage(newValue)
            )
            return emptyList()
        }

        override fun applyTo(config: SimulationConfiguration): SimulationConfiguration {
            return config.copy(
                amplifier = if (device is AmplifierDescriptor) config.amplifier.withValue(parameterId, newValue) else config.amplifier,
                pedalOne = if (device is SlotOnePedalDescriptor) config.pedalOne.withValue(parameterId, newValue) else config.pedalOne,
                pedalTwo = if (device is SlotTwoPedalDescriptor) config.pedalTwo.withValue(parameterId, newValue) else config.pedalTwo,
                reverbPedal = if (device is ReverbPedalDescriptor) config.reverbPedal.withValue(parameterId, newValue) else config.reverbPedal,
            )
        }

        override fun toString() = "${device.name} $parameterId = $newValue"
    }

    class DeviceType(
        val oldType: DeviceDescriptor<*>,
        val newType: DeviceDescriptor<*>,
    ) : ConfigurationDiff() {
        override suspend fun applyTo(ampClient: VoxVtxAmplifierClient): List<MessageToHost> {
            val updateMessage = newType.typeChangedMessage
            if (updateMessage is EffectPedalTypeChangedMessage) {
                return ampClient.exchange(updateMessage).dialUpdates
            } else {
                ampClient.exchange(updateMessage)
                return emptyList()
            }
        }

        override fun applyTo(config: SimulationConfiguration): SimulationConfiguration {
            return config.copy(
                amplifier = if (newType is AmplifierDescriptor) config.amplifier.withDescriptor(newType) else config.amplifier,
                pedalOne = if (newType is SlotOnePedalDescriptor) config.pedalOne.withDescriptor(newType) else config.pedalOne,
                pedalTwo = if (newType is SlotTwoPedalDescriptor) config.pedalTwo.withDescriptor(newType) else config.pedalTwo,
                reverbPedal = if (newType is ReverbPedalDescriptor) config.reverbPedal.withDescriptor(newType) else config.reverbPedal,
            )
        }

        override fun toString() = "exchanging ${oldType.name} for ${newType.name}"
    }
}

sealed class AmpStateUpdate {
    class Differential(val updates: List<ConfigurationDiff>) : AmpStateUpdate()
    class FullApply(val messagesToApply: List<MessageToAmp<*>>) : AmpStateUpdate()
}

fun VtxAmpState.diffTo(newState: VtxAmpState): AmpStateUpdate = diff(this, newState)
private fun diff(old: VtxAmpState, new: VtxAmpState): AmpStateUpdate {
    return when(old) {
        is VtxAmpState.ProgramSlotSelected -> {
            if (new !is VtxAmpState.ProgramSlotSelected) {
                TODO("switching operation mode is not supported")
            }

            if (old.slot == new.slot) {
                AmpStateUpdate.Differential(diff(old.activeConfiguration, new.activeConfiguration))
            } else {
                AmpStateUpdate.FullApply(listOf(
                    ProgramSlotChangedMessage(new.slot),
                    WriteUserProgramMessage(new.slot, new.activeConfiguration.toProtocolDataModel()),
                ))
            }
        }
        else -> TODO("switching operation mode is not supported")
    }
}

private fun diff(old: SimulationConfiguration, new: SimulationConfiguration): List<ConfigurationDiff> {
    return diff(old.amplifier, new.amplifier) +
            diff(old.pedalOne, new.pedalOne) +
            diff(old.pedalTwo, new.pedalTwo) +
            diff(old.reverbPedal, new.reverbPedal)
}

private fun diff(old: DeviceConfiguration<*>, new: DeviceConfiguration<*>): List<ConfigurationDiff> {
    // type change: no diff, possible, reset all values
    if (old.descriptor !== new.descriptor) {
        return listOf(
            ConfigurationDiff.DeviceType(old.descriptor, new.descriptor),
        ) + new.parameterValues.entries.map { (parameterId, newValue) ->
            @Suppress("UNCHECKED_CAST")
            ConfigurationDiff.Parameter(
                new.descriptor,
                parameterId as DeviceParameter.Id<Any>,
                newValue,
            )
        }
    }

    val oldValues = old.parameterValues
    val newValues = new.parameterValues
    check(oldValues.keys == newValues.keys) {
        "Identical device descriptors but different parameters from each configurations"
    }

    return oldValues.entries.mapNotNull { (parameterId, oldValue) ->
        @Suppress("UNCHECKED_CAST")
        parameterId as DeviceParameter.Id<Any>

        val newValue = newValues.getValue(parameterId)
        if (oldValue == newValue) {
            return@mapNotNull null
        }

        ConfigurationDiff.Parameter(
            new.descriptor,
            parameterId,
            newValue,
        )
    }
}

private val DeviceConfiguration<*>.parameterValues: Map<DeviceParameter.Id<*>, Any> get() = descriptor.parameters.associate {
    @Suppress("UNCHECKED_CAST")
    it as DeviceParameter<Any>
    it.id to getValue(it.id)
}