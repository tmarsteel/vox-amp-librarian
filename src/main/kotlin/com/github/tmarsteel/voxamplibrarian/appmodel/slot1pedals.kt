package com.github.tmarsteel.voxamplibrarian.appmodel

interface Slot1PedalDescriptor

object CompressorPedalDescriptor : Slot1PedalDescriptor, DeviceDescriptor {
    override val name = "Compressor"
    override val parameters = listOf(
        ContinuousUnitlessRangeParameter(DeviceParameter.Id.COMP_SENSITIVITY),
        ContinuousUnitlessRangeParameter(DeviceParameter.Id.PEDAL_LEVEL),
        ContinuousUnitlessRangeParameter(DeviceParameter.Id.COMP_ATTACK),
        DiscreteChoiceParameter<Voice>(DeviceParameter.Id.COMP_VOICE),
    )

    enum class Voice {
        ONE,
        TWO,
        THREE
    }
}

object ChorusPedalDescriptor : Slot1PedalDescriptor, DeviceDescriptor {
    override val name = "Chorus"
    override val parameters = listOf(
        ContinuousUnitlessRangeParameter(
            id = DeviceParameter.Id.CHORUS_SPEED,
            valueRange = 0x0064..0x4e10,
            semantic = ContinuousUnitlessRangeParameter.Semantic.FREQUENCY,
        ),
        ContinuousUnitlessRangeParameter(DeviceParameter.Id.CHORUS_DEPTH),
        ContinuousUnitlessRangeParameter(DeviceParameter.Id.CHORUS_MANUAL),
        ContinuousUnitlessRangeParameter(DeviceParameter.Id.PEDAL_MIX),
        ContinuousUnitlessRangeParameter(DeviceParameter.Id.EQ_LOW_CUT),
        ContinuousUnitlessRangeParameter(DeviceParameter.Id.EQ_HIGH_CUT),
    )
}

abstract class OverdrivePedalDescriptor(
    override val name: String,
) : Slot1PedalDescriptor, DeviceDescriptor {
    override val parameters = listOf(
        ContinuousUnitlessRangeParameter(DeviceParameter.Id.OVERDRIVE_DRIVE),
        ContinuousUnitlessRangeParameter(DeviceParameter.Id.EQ_TONE),
        ContinuousUnitlessRangeParameter(DeviceParameter.Id.PEDAL_LEVEL),
        ContinuousUnitlessRangeParameter(DeviceParameter.Id.EQ_TREBLE),
        ContinuousUnitlessRangeParameter(DeviceParameter.Id.EQ_MIDDLE),
        ContinuousUnitlessRangeParameter(DeviceParameter.Id.EQ_BASS),
    )
}

object TubeOdDescriptor : OverdrivePedalDescriptor("Tube Overdrive")
object GoldDriveDescriptor : OverdrivePedalDescriptor("Gold Drive")
object TrebleBoostDescriptor : OverdrivePedalDescriptor("Treble Boost")
object RcTurboDescriptor : OverdrivePedalDescriptor("RC Turbo")
object OrangeDistDescriptor : OverdrivePedalDescriptor("Orange Distortion")
object FatDistDescriptor : OverdrivePedalDescriptor("FAT Distortion")
object BritLeadDescriptor : OverdrivePedalDescriptor("Brit Lead")
object FuzzDescriptor : OverdrivePedalDescriptor("FUZZ")
