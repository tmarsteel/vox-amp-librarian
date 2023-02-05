package com.github.tmarsteel.voxamplibrarian.appmodel

abstract class ReverbPedalDescriptor(
    override val name: String,
) : DeviceDescriptor {
    override val parameters = listOf(
        ContinuousRangeParameter(DeviceParameter.Id.PEDAL_MIX),
        ContinuousRangeParameter(
            id = DeviceParameter.Id.REVERB_TIME,
            valueRange = 0..70,
            semantic = ContinuousRangeParameter.Semantic.TIME,
        ),
        ContinuousRangeParameter(DeviceParameter.Id.REVERB_PRE_DELAY),
        ContinuousRangeParameter(DeviceParameter.Id.REVERB_LOW_DAMP),
        ContinuousRangeParameter(DeviceParameter.Id.REVERB_HIGH_DAMP),
    )
}

object RoomReverbPedalDescriptor : ReverbPedalDescriptor("Room")
object SpringReverbPedalDescriptor : ReverbPedalDescriptor("Spring")
object HallReverbPedalDescriptor : ReverbPedalDescriptor("Hall")
object PlateReverbPedalDescriptor : ReverbPedalDescriptor("Plate")