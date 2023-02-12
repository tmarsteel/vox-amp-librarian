package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.appmodel.Frequency.Companion.mHz

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
    }
}

object CompressorPedalDescriptor : SlotOnePedalDescriptor {
    override val name = "Compressor"
    override val parameters = listOf(
        BooleanParameter(DeviceParameter.Id.PedalEnabled, false),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.CompSensitivity, 5.0),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.PedalLevel, 6.7),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.CompAttack, 5.7),
        DiscreteChoiceParameter(DeviceParameter.Id.CompVoice, Voice.TWO),
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
        BooleanParameter(DeviceParameter.Id.PedalEnabled, false),
        ContinuousRangeParameter(
            id = DeviceParameter.Id.ModulationSpeed,
            valueRange = 100.mHz .. 10_000.mHz,
            default = 100.mHz,
            valueFactory = ::Frequency,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.ModulationDepth, 6.7),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.ModulationManual, 5.7),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.PedalMix, 1.0),
        BooleanParameter(DeviceParameter.Id.EqLowCut, false),
        BooleanParameter(DeviceParameter.Id.EqHighCut, false),
    )
}

abstract class OverdrivePedalDescriptor(
    override val name: String,
) : SlotOnePedalDescriptor {
    override val parameters = listOf(
        BooleanParameter(DeviceParameter.Id.PedalEnabled, false),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.OverdriveDrive, 5.0),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.EqTone, 6.7),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.PedalLevel, 5.7),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.EqTreble, 5.0),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.EqMiddle, 5.0),
        ContinuousRangeParameter.zeroToTenUnitless(DeviceParameter.Id.EqBass, 5.0),
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
