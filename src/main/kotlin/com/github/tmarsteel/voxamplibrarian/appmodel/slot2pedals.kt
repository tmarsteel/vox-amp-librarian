package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.appmodel.Duration.Companion.ms
import com.github.tmarsteel.voxamplibrarian.appmodel.Frequency.Companion.mHz

sealed interface SlotTwoPedalDescriptor : DeviceDescriptor {
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
    }
}

object FlangerPedalDescriptor : SlotTwoPedalDescriptor {
    override val name = "Flanger"
    override val parameters = listOf(
        BooleanParameter(DeviceParameter.Id.PedalEnabled, false),
        ContinuousRangeParameter(
            id = DeviceParameter.Id.ModulationSpeed,
            valueRange = 100.mHz..5_000.mHz,
            default = 100.mHz,
            valueFactory = ::Frequency,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.ModulationDepth, 5.0),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.ModulationManual, 7.7),
        BooleanParameter(DeviceParameter.Id.EqLowCut, false),
        BooleanParameter(DeviceParameter.Id.EqHighCut, false),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.Resonance, 3.5),
    )
}

abstract class PhaserPedalDescriptor(
    override val name: String,
) : SlotTwoPedalDescriptor {
    override val parameters = listOf(
        BooleanParameter(DeviceParameter.Id.PedalEnabled, false),
        ContinuousRangeParameter(
            id = DeviceParameter.Id.ModulationSpeed,
            valueRange = 100.mHz..10_000.mHz,
            default = 100.mHz,
            valueFactory = ::Frequency,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.Resonance, 5.0),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.ModulationManual, 7.7),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.ModulationDepth, 0.0),
    )
}

object BlkPhaserDescriptor : PhaserPedalDescriptor("Blk Phaser")
object OrgPhaserOneDescriptor : PhaserPedalDescriptor("Orange Phaser I")
object OrgPhaserTwoDescriptor : PhaserPedalDescriptor("Orange Phaser II")

object TremoloPedalDescriptor : SlotTwoPedalDescriptor {
    override val name = "Tremolo"
    override val parameters = listOf(
        BooleanParameter(DeviceParameter.Id.PedalEnabled, false),
        ContinuousRangeParameter(
            id = DeviceParameter.Id.ModulationSpeed,
            valueRange = 1_650.mHz..10_000.mHz,
            default = 1_650.mHz,
            valueFactory = ::Frequency,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.ModulationDepth, 5.0),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.TremoloDuty, 7.7),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.TremoloShape, 0.0),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.PedalLevel, 1.0),
    )
}

abstract class DelayPedalDescriptor(
    override val name: String,
) : SlotTwoPedalDescriptor {
    override val parameters = listOf(
        BooleanParameter(DeviceParameter.Id.PedalEnabled, false),
        ContinuousRangeParameter(
            id = DeviceParameter.Id.DelayTime,
            valueRange = 30.ms..1_200.ms,
            default = 30.ms,
            valueFactory = ::Duration,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.PedalLevel, 5.0),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.DelayFeedback, 7.7),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.EqTone, 5.0),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.DelayModulationSpeed, 0.1),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.ModulationDepth, 0.0),
    )
}

object TapeEchoDescriptor : DelayPedalDescriptor("Tape Echo")
object AnalogDelayDescriptor : DelayPedalDescriptor("Analog Delay")