package com.github.tmarsteel.voxamplibrarian.appmodel

interface SlotOnePedalDescriptor : DeviceDescriptor

object CompressorPedalDescriptor : SlotOnePedalDescriptor {
    override val name = "Compressor"
    override val parameters = listOf(
        BooleanParameter(DeviceParameter.Id.PEDAL_ENABLED),
        ContinuousRangeParameter(DeviceParameter.Id.COMP_SENSITIVITY),
        ContinuousRangeParameter(DeviceParameter.Id.PEDAL_LEVEL),
        ContinuousRangeParameter(DeviceParameter.Id.COMP_ATTACK),
        DiscreteChoiceParameter<Voice>(DeviceParameter.Id.COMP_VOICE),
    )

    enum class Voice {
        ONE,
        TWO,
        THREE
    }
}

object ChorusPedalDescriptor : SlotOnePedalDescriptor {
    override val name = "Chorus"
    override val parameters = listOf(
        BooleanParameter(DeviceParameter.Id.PEDAL_ENABLED),
        ContinuousRangeParameter(
            id = DeviceParameter.Id.MODULATION_SPEED,
            valueRange = 100..10_000,
            semantic = ContinuousRangeParameter.Semantic.FREQUENCY,
        ),
        ContinuousRangeParameter(DeviceParameter.Id.MODULATION_DEPTH),
        ContinuousRangeParameter(DeviceParameter.Id.MODULATION_MANUAL),
        ContinuousRangeParameter(DeviceParameter.Id.PEDAL_MIX),
        ContinuousRangeParameter(DeviceParameter.Id.EQ_LOW_CUT),
        ContinuousRangeParameter(DeviceParameter.Id.EQ_HIGH_CUT),
    )
}

abstract class OverdrivePedalDescriptor(
    override val name: String,
) : SlotOnePedalDescriptor {
    override val parameters = listOf(
        BooleanParameter(DeviceParameter.Id.PEDAL_ENABLED),
        ContinuousRangeParameter(DeviceParameter.Id.OVERDRIVE_DRIVE),
        ContinuousRangeParameter(DeviceParameter.Id.EQ_TONE),
        ContinuousRangeParameter(DeviceParameter.Id.PEDAL_LEVEL),
        ContinuousRangeParameter(DeviceParameter.Id.EQ_TREBLE),
        ContinuousRangeParameter(DeviceParameter.Id.EQ_MIDDLE),
        ContinuousRangeParameter(DeviceParameter.Id.EQ_BASS),
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
