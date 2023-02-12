package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.appmodel.Duration.Companion.ms

sealed class ReverbPedalDescriptor(
    override val name: String,
) : DeviceDescriptor {
    override val parameters = listOf(
        BooleanParameter(DeviceParameter.Id.PedalEnabled, false),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.PedalMix, 7.5),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.ReverbTime, 4.5),
        ContinuousRangeParameter(
            id = DeviceParameter.Id.ReverbPreDelay,
            valueRange = 0.ms .. 70.ms,
            default = 0.ms,
            valueFactory = ::Duration,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.ReverbLowDamp, 3.6),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.ReverbHighDamp, 2.5),
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

object RoomReverbPedalDescriptor : ReverbPedalDescriptor("Room")
object SpringReverbPedalDescriptor : ReverbPedalDescriptor("Spring")
object HallReverbPedalDescriptor : ReverbPedalDescriptor("Hall")
object PlateReverbPedalDescriptor : ReverbPedalDescriptor("Plate")