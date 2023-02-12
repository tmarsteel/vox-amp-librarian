package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.appmodel.Duration.Companion.ms

abstract class ReverbPedalDescriptor(
    override val name: String,
) : DeviceDescriptor {
    override val parameters = listOf(
        BooleanParameter(DeviceParameter.Id.PedalEnabled, false),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.PedalMix, 7.5),
        ContinuousRangeParameter(
            id = DeviceParameter.Id.ReverbTime,
            valueRange = (0.ms) .. (100.ms),
            default = 45.ms,
            valueFactory = ::Duration
        ),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.ReverbPreDelay, 0.0),
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