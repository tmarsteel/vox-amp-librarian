package com.github.tmarsteel.voxamplibrarian.appmodel

interface SlotOnePedalDescriptor : DeviceDescriptor {
    companion object {
        val ALL = listOf(
            CompressorPedalDescriptor,
            ChorusPedalDescriptor,
            TubeOdDescriptor,
            GoldDriveDescriptor,
            TrebleBoostDescriptor,
            RcTurboDescriptor,
            OrangeDistDescriptor,
            FatDistDescriptor,
            BritLeadDescriptor,
            FuzzDescriptor,
        )
        val DEFAULT = DeviceConfiguration<SlotOnePedalDescriptor>(
            CompressorPedalDescriptor,
            CompressorPedalDescriptor.defaults,
        )
    }
}

object CompressorPedalDescriptor : SlotOnePedalDescriptor {
    override val name = "Compressor"
    override val parameters = listOf(
        BooleanParameter(DeviceParameter.Id.PEDAL_ENABLED),
        ContinuousRangeParameter(DeviceParameter.Id.COMP_SENSITIVITY),
        ContinuousRangeParameter(DeviceParameter.Id.PEDAL_LEVEL),
        ContinuousRangeParameter(DeviceParameter.Id.COMP_ATTACK),
        DiscreteChoiceParameter<Voice>(DeviceParameter.Id.COMP_VOICE),
    )

    override val defaults = mapOf(
        DeviceParameter.Id.PEDAL_ENABLED to false,
        DeviceParameter.Id.COMP_SENSITIVITY to 50,
        DeviceParameter.Id.PEDAL_LEVEL to 67,
        DeviceParameter.Id.COMP_ATTACK to 57,
        DeviceParameter.Id.COMP_VOICE to Voice.TWO,
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
        BooleanParameter(DeviceParameter.Id.EQ_LOW_CUT),
        BooleanParameter(DeviceParameter.Id.EQ_HIGH_CUT),
    )
    override val defaults = mapOf(
        DeviceParameter.Id.PEDAL_ENABLED to false,
        DeviceParameter.Id.MODULATION_SPEED to 100,
        DeviceParameter.Id.MODULATION_DEPTH to 67,
        DeviceParameter.Id.MODULATION_MANUAL to 57,
        DeviceParameter.Id.PEDAL_MIX to 1,
        DeviceParameter.Id.EQ_LOW_CUT to false,
        DeviceParameter.Id.EQ_HIGH_CUT to false,
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

    override val defaults = mapOf(
        DeviceParameter.Id.PEDAL_ENABLED to false,
        DeviceParameter.Id.OVERDRIVE_DRIVE to 50,
        DeviceParameter.Id.EQ_TONE to 67,
        DeviceParameter.Id.PEDAL_LEVEL to 57,
        DeviceParameter.Id.EQ_TREBLE to 50,
        DeviceParameter.Id.EQ_MIDDLE to 50,
        DeviceParameter.Id.EQ_BASS to 50,
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
