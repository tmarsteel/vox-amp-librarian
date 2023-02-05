package com.github.tmarsteel.voxamplibrarian.appmodel

interface SlotTwoPedalDescriptor : DeviceDescriptor

object FlangerPedalDescriptor : SlotTwoPedalDescriptor {
    override val name = "Flanger"
    override val parameters = listOf(
        BooleanParameter(DeviceParameter.Id.PEDAL_ENABLED),
        ContinuousRangeParameter(
            id = DeviceParameter.Id.MODULATION_SPEED,
            valueRange = 100..5_000,
            semantic = ContinuousRangeParameter.Semantic.FREQUENCY,
        ),
        ContinuousRangeParameter(DeviceParameter.Id.MODULATION_DEPTH),
        ContinuousRangeParameter(DeviceParameter.Id.MODULATION_MANUAL),
        ContinuousRangeParameter(DeviceParameter.Id.EQ_LOW_CUT),
        ContinuousRangeParameter(DeviceParameter.Id.EQ_HIGH_CUT),
        ContinuousRangeParameter(DeviceParameter.Id.RESONANCE),
    )
}

abstract class PhaserPedalDescriptor(
    override val name: String,
) : SlotTwoPedalDescriptor {
    override val parameters = listOf(
        BooleanParameter(DeviceParameter.Id.PEDAL_ENABLED),
        ContinuousRangeParameter(
            id = DeviceParameter.Id.MODULATION_SPEED,
            valueRange = 100..10_000,
            semantic = ContinuousRangeParameter.Semantic.FREQUENCY,
        ),
        ContinuousRangeParameter(DeviceParameter.Id.RESONANCE),
        ContinuousRangeParameter(DeviceParameter.Id.MODULATION_MANUAL),
        ContinuousRangeParameter(DeviceParameter.Id.MODULATION_DEPTH),
    )
}

object BlkPhaserDescriptor : PhaserPedalDescriptor("Blk Phaser")
object OrgPhaserOneDescriptor : PhaserPedalDescriptor("Orange Phaser I")
object OrgPhaserTwoDescriptor : PhaserPedalDescriptor("Orange Phaser II")

object TremoloPedalDescriptor : SlotTwoPedalDescriptor {
    override val name = "Tremolo"
    override val parameters = listOf(
        BooleanParameter(DeviceParameter.Id.PEDAL_ENABLED),
        ContinuousRangeParameter(
            id = DeviceParameter.Id.MODULATION_SPEED,
            valueRange = 1_650..10_000,
            semantic = ContinuousRangeParameter.Semantic.FREQUENCY,
        ),
        ContinuousRangeParameter(DeviceParameter.Id.MODULATION_DEPTH),
        ContinuousRangeParameter(DeviceParameter.Id.TREMOLO_DUTY),
        ContinuousRangeParameter(DeviceParameter.Id.TREMOLO_SHAPE),
        ContinuousRangeParameter(DeviceParameter.Id.PEDAL_LEVEL),
    )
}

abstract class DelayPedalDescriptor(
    override val name: String,
) : SlotTwoPedalDescriptor {
    override val parameters = listOf(
        BooleanParameter(DeviceParameter.Id.PEDAL_ENABLED),
        ContinuousRangeParameter(
            id = DeviceParameter.Id.DELAY_TIME,
            valueRange = 30..1_200,
            semantic = ContinuousRangeParameter.Semantic.TIME,
        ),
        ContinuousRangeParameter(DeviceParameter.Id.PEDAL_LEVEL),
        ContinuousRangeParameter(DeviceParameter.Id.DELAY_FEEDBACK),
        ContinuousRangeParameter(DeviceParameter.Id.EQ_TONE),
        ContinuousRangeParameter(DeviceParameter.Id.MODULATION_SPEED),
        ContinuousRangeParameter(DeviceParameter.Id.MODULATION_DEPTH),
    )
}

object TapeEchoDescriptor : DelayPedalDescriptor("Tape Echo")
object AnalogDelayDescriptor : DelayPedalDescriptor("Analog Delay")