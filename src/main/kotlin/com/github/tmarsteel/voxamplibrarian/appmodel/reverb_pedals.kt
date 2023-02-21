package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.appmodel.Duration.Companion.ms
import com.github.tmarsteel.voxamplibrarian.protocol.MutableProgram
import com.github.tmarsteel.voxamplibrarian.protocol.Program
import com.github.tmarsteel.voxamplibrarian.protocol.ReverbPedalType

sealed class ReverbPedalDescriptor(
    override val name: String,
    override val pedalType: ReverbPedalType,
) : PedalDescriptor<ReverbPedalDescriptor> {
    override fun applyTypeToProgram(program: MutableProgram) {
        program.reverbPedalType = pedalType
    }

    override fun isContainedIn(program: Program): Boolean {
        return program.reverbPedalType == pedalType
    }

    override val parameters = listOf(
        BooleanParameter(
            DeviceParameter.Id.PedalEnabled,
            pedalEnabledSwitch(),
            false,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.PedalMix,
            unitlessPedalDial(0x00, MutableProgram::reverbPedalDial1),
            7.5
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.ReverbTime,
            unitlessPedalDial(0x01, MutableProgram::reverbPedalDial2),
            4.5
        ),
        ContinuousRangeParameter(
            id = DeviceParameter.Id.ReverbPreDelay,
            protocolAdapter = durationPedalDial(0x02, MutableProgram::reverbPedalDial3),
            valueRange = 0.ms .. 70.ms,
            default = 0.ms,
            valueFactory = ::Duration,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.ReverbLowDamp,
            unitlessPedalDial(0x03, MutableProgram::reverbPedalDial4),
            3.6
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.ReverbHighDamp,
            unitlessPedalDial(0x04, MutableProgram::reverbPedalDial5),
            2.5
        ),
    )

    companion object {
        val ALL = listOf(
            RoomReverbPedalDescriptor,
            SpringReverbPedalDescriptor,
            HallReverbPedalDescriptor,
            PlateReverbPedalDescriptor,
        )
    }
}

object RoomReverbPedalDescriptor : ReverbPedalDescriptor("Room", ReverbPedalType.ROOM)
object SpringReverbPedalDescriptor : ReverbPedalDescriptor("Spring", ReverbPedalType.SPRING)
object HallReverbPedalDescriptor : ReverbPedalDescriptor("Hall", ReverbPedalType.HALL)
object PlateReverbPedalDescriptor : ReverbPedalDescriptor("Plate" ,ReverbPedalType.PLATE)