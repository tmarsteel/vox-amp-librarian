package com.github.tmarsteel.voxamplibrarian.appmodel

abstract class ReverbPedalDescriptor(
    override val name: String,
) : DeviceDescriptor {
    override val parameters = listOf(
        BooleanParameter(DeviceParameter.Id.PEDAL_ENABLED),
        ContinuousRangeParameter(DeviceParameter.Id.PEDAL_MIX),
        ContinuousRangeParameter(
            id = DeviceParameter.Id.REVERB_TIME,
            valueRange = 0..100,
            semantic = ContinuousRangeParameter.Semantic.TIME,
        ),
        ContinuousRangeParameter(DeviceParameter.Id.REVERB_PRE_DELAY),
        ContinuousRangeParameter(DeviceParameter.Id.REVERB_LOW_DAMP),
        ContinuousRangeParameter(DeviceParameter.Id.REVERB_HIGH_DAMP),
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