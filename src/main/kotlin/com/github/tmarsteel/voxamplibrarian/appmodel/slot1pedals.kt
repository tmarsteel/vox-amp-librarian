package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.appmodel.Frequency.Companion.mHz
import com.github.tmarsteel.voxamplibrarian.protocol.MutableProgram
import com.github.tmarsteel.voxamplibrarian.protocol.Program
import com.github.tmarsteel.voxamplibrarian.protocol.SingleByteProtocolSerializable
import com.github.tmarsteel.voxamplibrarian.protocol.Slot1PedalType

sealed interface SlotOnePedalDescriptor : PedalDescriptor<SlotOnePedalDescriptor> {
    override val pedalType: Slot1PedalType
    override fun applyTypeToProgram(program: MutableProgram) {
        program.pedal1Type = pedalType
    }

    override fun isContainedIn(program: Program): Boolean {
        return program.pedal1Type == pedalType
    }

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
    override val pedalType = Slot1PedalType.COMP
    override val parameters = listOf(
        BooleanParameter(
            DeviceParameter.Id.PedalEnabled,
            pedalEnabledSwitch(),
            false
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.CompSensitivity,
            unitlessPedalDial(0x00, MutableProgram::pedal1Dial1),
            5.0
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.PedalLevel,
            unitlessPedalDial(0x01, MutableProgram::pedal1Dial2),
            6.7,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.CompAttack,
            unitlessPedalDial(0x02, MutableProgram::pedal1Dial3),
            5.7,
        ),
        DiscreteChoiceParameter(
            DeviceParameter.Id.CompVoice,
            pedalSelector(0x03, MutableProgram::pedal1Dial4),
            Voice.TWO,
        ),
    )

    enum class Voice(override val protocolValue: Byte) : SingleByteProtocolSerializable {
        ONE(0x00),
        TWO(0x01),
        THREE(0x02),
    }
}

object ChorusPedalDescriptor : SlotOnePedalDescriptor {
    override val name = "Chorus"
    override val pedalType = Slot1PedalType.CHORUS
    override val parameters = listOf(
        BooleanParameter(
            DeviceParameter.Id.PedalEnabled,
            pedalEnabledSwitch(),
            false
        ),
        ContinuousRangeParameter(
            id = DeviceParameter.Id.ModulationSpeed,
            protocolAdapter = frequencyPedalDial(0x00, MutableProgram::pedal1Dial1),
            valueRange = 100.mHz .. 10_000.mHz,
            default = 100.mHz,
            valueFactory = ::Frequency,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.ModulationDepth,
            unitlessPedalDial(0x01, MutableProgram::pedal2Dial2),
            6.7,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.ModulationManual,
            unitlessPedalDial(0x02, MutableProgram::pedal2Dial3),
            5.7,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.PedalMix,
            unitlessPedalDial(0x03, MutableProgram::pedal2Dial4),
            1.0,
        ),
        BooleanParameter(
            DeviceParameter.Id.EqLowCut,
            pedalSwitch(0x04, MutableProgram::pedal1Dial5),
            false
        ),
        BooleanParameter(
            DeviceParameter.Id.EqHighCut,
            pedalSwitch(0x05, MutableProgram::pedal1Dial6),
            false
        ),
    )
}

sealed class OverdrivePedalDescriptor(
    override val name: String,
    override val pedalType: Slot1PedalType,
) : SlotOnePedalDescriptor {
    override val parameters = listOf(
        BooleanParameter(
            DeviceParameter.Id.PedalEnabled,
            pedalEnabledSwitch(),
            false,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.OverdriveDrive,
            unitlessPedalDial(0x00, MutableProgram::pedal1Dial1),
            5.0,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.EqTone,
            unitlessPedalDial(0x01, MutableProgram::pedal1Dial2),
            6.7,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.PedalLevel,
            unitlessPedalDial(0x02, MutableProgram::pedal1Dial3),
            5.7,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.EqTreble,
            unitlessPedalDial(0x03, MutableProgram::pedal1Dial4),
            5.0,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.EqMiddle,
            unitlessPedalDial(0x04, MutableProgram::pedal1Dial5),
            5.0,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.EqBass,
            unitlessPedalDial(0x05, MutableProgram::pedal1Dial6),
            5.0,
        ),
    )
}

object TubeOdDescriptor : OverdrivePedalDescriptor("Tube Overdrive", Slot1PedalType.OVERDRIVE)
object GoldDriveDescriptor : OverdrivePedalDescriptor("Gold Drive", Slot1PedalType.GOLD_DRIVE)
object TrebleBoostDescriptor : OverdrivePedalDescriptor("Treble Boost", Slot1PedalType.TREBLE_BOOST)
object RcTurboDescriptor : OverdrivePedalDescriptor("RC Turbo", Slot1PedalType.RC_TURBO)
object OrangeDistDescriptor : OverdrivePedalDescriptor("Orange Distortion", Slot1PedalType.ORANGE_DIST)
object FatDistDescriptor : OverdrivePedalDescriptor("FAT Distortion", Slot1PedalType.FAT_DIST)
object BritLeadDescriptor : OverdrivePedalDescriptor("Brit Lead", Slot1PedalType.BRIT_LEAD)
object FuzzDescriptor : OverdrivePedalDescriptor("FUZZ", Slot1PedalType.FUZZ)