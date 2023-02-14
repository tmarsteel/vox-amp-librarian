package com.github.tmarsteel.voxamplibrarian.appmodel

import com.github.tmarsteel.voxamplibrarian.appmodel.Duration.Companion.ms
import com.github.tmarsteel.voxamplibrarian.appmodel.Frequency.Companion.mHz
import com.github.tmarsteel.voxamplibrarian.protocol.MutableProgram
import com.github.tmarsteel.voxamplibrarian.protocol.Program
import com.github.tmarsteel.voxamplibrarian.protocol.Slot2PedalType

sealed interface SlotTwoPedalDescriptor : PedalDescriptor<SlotTwoPedalDescriptor> {
    override val pedalType: Slot2PedalType

    override fun applyTypeToProgram(program: MutableProgram) {
        program.pedal2Type = pedalType
    }

    override fun isContainedIn(program: Program): Boolean {
        return program.pedal2Type == pedalType
    }

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
    override val pedalType = Slot2PedalType.FLANGER
    override val parameters = listOf(
        BooleanParameter(
            DeviceParameter.Id.PedalEnabled,
            pedalEnabledSwitch(),
        false,
        ),
        ContinuousRangeParameter(
            id = DeviceParameter.Id.ModulationSpeed,
            protocolAdapter = frequencyPedalDial(0x00, MutableProgram::pedal2Dial1),
            valueRange = 100.mHz..5_000.mHz,
            default = 100.mHz,
            valueFactory = ::Frequency,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.ModulationDepth,
            unitlessPedalDial(0x01, MutableProgram::pedal2Dial2),
            5.0,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.ModulationManual,
            unitlessPedalDial(0x02, MutableProgram::pedal2Dial3),
            7.7,
        ),
        BooleanParameter(
            DeviceParameter.Id.EqLowCut,
            pedalSwitch(0x03, MutableProgram::pedal2Dial4),
            false,
        ),
        BooleanParameter(
            DeviceParameter.Id.EqHighCut,
            pedalSwitch(0x04, MutableProgram::pedal2Dial5),
            false,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.Resonance,
            unitlessPedalDial(0x05, MutableProgram::pedal2Dial6),
            3.5,
        ),
    )
}

sealed class PhaserPedalDescriptor(
    override val name: String,
    override val pedalType: Slot2PedalType,
) : SlotTwoPedalDescriptor {
    override val parameters = listOf(
        BooleanParameter(
            DeviceParameter.Id.PedalEnabled,
            pedalEnabledSwitch(),
            false,
        ),
        ContinuousRangeParameter(
            id = DeviceParameter.Id.ModulationSpeed,
            protocolAdapter = frequencyPedalDial(0x00, MutableProgram::pedal2Dial1),
            valueRange = 100.mHz..10_000.mHz,
            default = 100.mHz,
            valueFactory = ::Frequency,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.Resonance,
            unitlessPedalDial(0x01, MutableProgram::pedal2Dial2),
            5.0,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.ModulationManual,
            unitlessPedalDial(0x02, MutableProgram::pedal2Dial3),
            7.7,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.ModulationDepth,
            unitlessPedalDial(0x03, MutableProgram::pedal2Dial4),
            0.0,
        ),
    )
}

object BlkPhaserDescriptor : PhaserPedalDescriptor("Blk Phaser", Slot2PedalType.BLK_PHASER)
object OrgPhaserOneDescriptor : PhaserPedalDescriptor("Orange Phaser I", Slot2PedalType.ORG_PHASER_1)
object OrgPhaserTwoDescriptor : PhaserPedalDescriptor("Orange Phaser II", Slot2PedalType.ORG_PHASER_2)

object TremoloPedalDescriptor : SlotTwoPedalDescriptor {
    override val name = "Tremolo"
    override val pedalType = Slot2PedalType.TREMOLO
    override val parameters = listOf(
        BooleanParameter(
            DeviceParameter.Id.PedalEnabled,
            pedalEnabledSwitch(),
            false
        ),
        ContinuousRangeParameter(
            id = DeviceParameter.Id.ModulationSpeed,
            protocolAdapter = frequencyPedalDial(0x00, MutableProgram::pedal2Dial1),
            valueRange = 1_650.mHz..10_000.mHz,
            default = 1_650.mHz,
            valueFactory = ::Frequency,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.ModulationDepth,
            unitlessPedalDial(0x01, MutableProgram::pedal2Dial2),
            5.0
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.TremoloDuty,
            unitlessPedalDial(0x02, MutableProgram::pedal2Dial3),
            7.7
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.TremoloShape,
            unitlessPedalDial(0x03, MutableProgram::pedal2Dial4),
            0.0
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.PedalLevel,
            unitlessPedalDial(0x04, MutableProgram::pedal2Dial5),
            1.0
        ),
    )
}

sealed class DelayPedalDescriptor(
    override val name: String,
    override val pedalType: Slot2PedalType,
) : SlotTwoPedalDescriptor {
    override val parameters = listOf(
        BooleanParameter(
            DeviceParameter.Id.PedalEnabled,
            pedalEnabledSwitch(),
            false
        ),
        ContinuousRangeParameter(
            id = DeviceParameter.Id.DelayTime,
            protocolAdapter = durationPedalDial(0x00, MutableProgram::pedal2Dial1),
            valueRange = 30.ms..1_200.ms,
            default = 30.ms,
            valueFactory = ::Duration,
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.PedalLevel,
            unitlessPedalDial(0x01, MutableProgram::pedal2Dial2),
            5.0
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.DelayFeedback,
            unitlessPedalDial(0x02, MutableProgram::pedal2Dial3),
            7.7
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.EqTone,
            unitlessPedalDial(0x04, MutableProgram::pedal2Dial4),
            5.0
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.DelayModulationSpeed,
            unitlessPedalDial(0x05, MutableProgram::pedal2Dial5),
            0.1
        ),
        ContinuousRangeParameter.zeroToTenUnitless(
            DeviceParameter.Id.ModulationDepth,
            unitlessPedalDial(0x06, MutableProgram::pedal2Dial6),
            0.0
        ),
    )
}

object TapeEchoDescriptor : DelayPedalDescriptor("Tape Echo", Slot2PedalType.TAPE_ECHO)
object AnalogDelayDescriptor : DelayPedalDescriptor("Analog Delay", Slot2PedalType.ANALOG_DELAY)