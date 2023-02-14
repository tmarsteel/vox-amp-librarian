package com.github.tmarsteel.voxamplibrarian.appmodel.hardware_integration

import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceConfiguration
import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceParameter
import com.github.tmarsteel.voxamplibrarian.appmodel.SimulationConfiguration
import com.github.tmarsteel.voxamplibrarian.appmodel.VtxAmpState
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
import com.github.tmarsteel.voxamplibrarian.protocol.ZeroToTenDial
import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageToAmp
import com.github.tmarsteel.voxamplibrarian.protocol.message.ProgramSlotChangedMessage
import com.github.tmarsteel.voxamplibrarian.protocol.message.WriteUserProgramMessage

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
    abstract fun toUpdateMessage(): MessageToAmp<*>

    class Parameter<V : Any>(
        val device: DeviceDescriptor<*>,
        val parameterId: DeviceParameter.Id<V>,
        val newValue: V,
    ): ConfigurationDiff() {
        override fun toUpdateMessage(): MessageToAmp<*> {
            return device.getParameter(parameterId).buildUpdateMessage(newValue)
        }
    }

    class DeviceType(
        val oldType: DeviceDescriptor<*>,
        val newType: DeviceDescriptor<*>,
    ) : ConfigurationDiff() {
        override fun toUpdateMessage() = newType.typeChangedMessage
    }
}

fun VtxAmpState.diffTo(newState: VtxAmpState): List<MessageToAmp<*>> = diff(this, newState)
private fun diff(old: VtxAmpState, new: VtxAmpState): List<MessageToAmp<*>> {
    return when(old) {
        is VtxAmpState.ProgramSlotSelected -> {
            if (new !is VtxAmpState.ProgramSlotSelected) {
                TODO("switching operation mode is not supported")
            }

            if (old.slot == new.slot) {
                diff(old, new)
            } else {
                listOf(
                    ProgramSlotChangedMessage(new.slot),
                    WriteUserProgramMessage(new.slot, new.configuration.toProtocolDataModel()),
                )
            }
        }
        else -> TODO()
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
        if (oldValue != newValue) {
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