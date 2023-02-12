package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.appmodel.ParameterValue.Companion.withValue
import kotlin.time.Duration.Companion.milliseconds

abstract class ReverbPedalDescriptor(
    name: String,
) : DeviceDescriptor(name, listOf(
        BooleanParameter(DeviceParameter.Id.PedalEnabled) withValue false,
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.PedalMix) withValue UnitlessSingleDecimalPrecision(75),
        ContinuousRangeParameter(
            id = DeviceParameter.Id.ReverbTime,
            valueRange = (0.milliseconds) .. (100.milliseconds),
        ),
        ContinuousRangeParameter(DeviceParameter.Id.ReverbPreDelay) withValue 0,
        ContinuousRangeParameter(DeviceParameter.Id.ReverbLowDamp) withValue 36,
        ContinuousRangeParameter(DeviceParameter.Id.ReverbHighDamp) withValue 25,
    )

    override val defaults = mapOf(
        DeviceParameter.Id.PEDAL_ENABLED to false,
        DeviceParameter.Id.PEDAL_MIX to 75,
        DeviceParameter.Id.REVERB_TIME to 45,
        DeviceParameter.Id.REVERB_PRE_DELAY to 0,
        DeviceParameter.Id.REVERB_LOW_DAMP to 36,
        DeviceParameter.Id.REVERB_HIGH_DAMP to 25,
    )

    companion object {
        val ALL = listOf(
            RoomReverbPedalDescriptor,
            SpringReverbPedalDescriptor,
            HallReverbPedalDescriptor,
            PlateReverbPedalDescriptor,
        )
        val DEFAULT = DeviceConfiguration<ReverbPedalDescriptor>(
            RoomReverbPedalDescriptor,
            RoomReverbPedalDescriptor.defaults,
        )
    }
}

object RoomReverbPedalDescriptor : ReverbPedalDescriptor("Room")
object SpringReverbPedalDescriptor : ReverbPedalDescriptor("Spring")
object HallReverbPedalDescriptor : ReverbPedalDescriptor("Hall")
object PlateReverbPedalDescriptor : ReverbPedalDescriptor("Plate")