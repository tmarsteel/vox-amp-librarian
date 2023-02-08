package com.github.tmarsteel.voxamplibrarian.appmodel

interface SlotTwoPedalDescriptor : DeviceDescriptor {
    companion object {
        val ALL = listOf(
            FlangerPedalDescriptor,
            BlkPhaserDescriptor,
            OrgPhaserOneDescriptor,
            OrgPhaserTwoDescriptor,
            TremoloPedalDescriptor,
            TapeEchoDescriptor,
            AnalogDelayDescriptor,
        )
        val DEFAULT = DeviceConfiguration<SlotTwoPedalDescriptor>(
            FlangerPedalDescriptor,
            FlangerPedalDescriptor.defaults,
        )
    }
}

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
        BooleanParameter(DeviceParameter.Id.EQ_LOW_CUT),
        BooleanParameter(DeviceParameter.Id.EQ_HIGH_CUT),
        ContinuousRangeParameter(DeviceParameter.Id.RESONANCE),
    )

    override val defaults = mapOf(
        DeviceParameter.Id.PEDAL_ENABLED to false,
        DeviceParameter.Id.MODULATION_SPEED to 100,
        DeviceParameter.Id.MODULATION_DEPTH to 50,
        DeviceParameter.Id.MODULATION_MANUAL to 77,
        DeviceParameter.Id.EQ_LOW_CUT to false,
        DeviceParameter.Id.EQ_HIGH_CUT to false,
        DeviceParameter.Id.RESONANCE to 35,
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

    override val defaults = mapOf(
        DeviceParameter.Id.PEDAL_ENABLED to false,
        DeviceParameter.Id.MODULATION_SPEED to 100,
        DeviceParameter.Id.RESONANCE to 50,
        DeviceParameter.Id.MODULATION_MANUAL to 77,
        DeviceParameter.Id.MODULATION_DEPTH to 0,
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

    override val defaults = mapOf(
        DeviceParameter.Id.PEDAL_ENABLED to false,
        DeviceParameter.Id.MODULATION_SPEED to 1650,
        DeviceParameter.Id.MODULATION_DEPTH to 50,
        DeviceParameter.Id.TREMOLO_DUTY to 77,
        DeviceParameter.Id.TREMOLO_SHAPE to 0,
        DeviceParameter.Id.PEDAL_LEVEL to 1,
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

    override val defaults = mapOf(
        DeviceParameter.Id.PEDAL_ENABLED to false,
        DeviceParameter.Id.DELAY_TIME to 30,
        DeviceParameter.Id.PEDAL_LEVEL to 50,
        DeviceParameter.Id.DELAY_FEEDBACK to 77,
        DeviceParameter.Id.EQ_TONE to 50,
        DeviceParameter.Id.MODULATION_SPEED to 10,
        DeviceParameter.Id.MODULATION_DEPTH to 0,
    )
}

object TapeEchoDescriptor : DelayPedalDescriptor("Tape Echo")
object AnalogDelayDescriptor : DelayPedalDescriptor("Analog Delay")