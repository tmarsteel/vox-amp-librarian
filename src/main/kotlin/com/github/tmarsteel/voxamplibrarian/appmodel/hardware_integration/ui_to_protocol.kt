package com.github.tmarsteel.voxamplibrarian.appmodel.hardware_integration

import com.github.tmarsteel.voxamplibrarian.appmodel.AmplifierDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.AnalogDelayDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.BlkPhaserDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.BoutiqueClAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.BoutiqueMetalAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.BoutiqueOdAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.Brit1959NormalAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.Brit1959TrebleAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.Brit800Amplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.BritLeadDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.BritOrMkTwoAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.BritVmAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.CaliElationAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.ChorusPedalDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.CompressorPedalDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.Continuous
import com.github.tmarsteel.voxamplibrarian.appmodel.DelayPedalDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.DeluxeClNormalAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.DeluxeClVibratoAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceConfiguration
import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceParameter
import com.github.tmarsteel.voxamplibrarian.appmodel.DoubleRecAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.EruptThreeChannelThreeAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.EruptThreeChannelTwoAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.FatDistDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.FlangerPedalDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.FuzzDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.GoldDriveDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.HallReverbPedalDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.OrangeDistDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.OrgPhaserOneDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.OrgPhaserTwoDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.OriginalCleanAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.OverdrivePedalDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.PhaserPedalDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.PlateReverbPedalDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.RcTurboDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.ReverbPedalDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.RoomReverbPedalDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.SimulationConfiguration
import com.github.tmarsteel.voxamplibrarian.appmodel.SlOdAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.SlotOnePedalDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.SlotTwoPedalDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.SpringReverbPedalDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.TapeEchoDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.TrebleBoostDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.TremoloPedalDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.TubeOdDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.Tweed4X10BrightAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.Tweed4X10NormalAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.UnitlessSingleDecimalPrecision
import com.github.tmarsteel.voxamplibrarian.appmodel.VoxAc30Amplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.VoxAc30TbAmplifier
import com.github.tmarsteel.voxamplibrarian.protocol.AmpModel
import com.github.tmarsteel.voxamplibrarian.protocol.Program
import com.github.tmarsteel.voxamplibrarian.protocol.ProgramName
import com.github.tmarsteel.voxamplibrarian.protocol.ReverbPedalType
import com.github.tmarsteel.voxamplibrarian.protocol.Slot1PedalType
import com.github.tmarsteel.voxamplibrarian.protocol.Slot2PedalType
import com.github.tmarsteel.voxamplibrarian.protocol.TwoByteDial
import com.github.tmarsteel.voxamplibrarian.protocol.ZeroToTenDial

private fun UnitlessSingleDecimalPrecision.toZeroToTenDial() = ZeroToTenDial(intValue.toByte())
private val AmplifierDescriptor.model: AmpModel
    get() = when(this) {
    DeluxeClVibratoAmplifier -> AmpModel.DELUXE_CL_VIBRATO
    DeluxeClNormalAmplifier -> AmpModel.DELUXE_CL_NORMAL
    Tweed4X10BrightAmplifier -> AmpModel.TWEED_410_BRIGHT
    Tweed4X10NormalAmplifier -> AmpModel.TWEED_410_NORMAL
    BoutiqueClAmplifier -> AmpModel.BOUTIQUE_CL
    BoutiqueOdAmplifier -> AmpModel.BOUTIQUE_OD
    VoxAc30Amplifier -> AmpModel.VOX_AC30
    VoxAc30TbAmplifier -> AmpModel.VOX_AC30TB
    Brit1959TrebleAmplifier -> AmpModel.BRIT_1959_TREBLE
    Brit1959NormalAmplifier -> AmpModel.BRIT_1959_NORMAL
    Brit800Amplifier -> AmpModel.BRIT_800
    BritVmAmplifier -> AmpModel.BRIT_VM
    SlOdAmplifier -> AmpModel.SL_OD
    DoubleRecAmplifier -> AmpModel.DOUBLE_REC
    CaliElationAmplifier -> AmpModel.CALI_ELATION
    EruptThreeChannelTwoAmplifier -> AmpModel.ERUPT_III_CH2
    EruptThreeChannelThreeAmplifier -> AmpModel.ERUPT_III_CH3
    BoutiqueMetalAmplifier -> AmpModel.BOUTIQUE_METAL
    BritOrMkTwoAmplifier -> AmpModel.BRIT_OR_MKII
    OriginalCleanAmplifier -> AmpModel.ORIGINAL_CL
    else -> error("Unknown amplifier type: ${this::class.simpleName}")
}

private fun CompressorPedalDescriptor.Voice.toProtocolDataModel(): Byte = when(this) {
    CompressorPedalDescriptor.Voice.ONE -> 0x01
    CompressorPedalDescriptor.Voice.TWO -> 0x02
    CompressorPedalDescriptor.Voice.THREE -> 0x03
}

private fun Continuous<*>.toTwoByteDial() = TwoByteDial(intValue.toUShort())
private fun Boolean.toByte(): Byte = if (this) 0x01 else 0x00

internal fun SimulationConfiguration.toProtocolDataModel(): Program {
    return Program(
        ProgramName(programName ?: ""),
        amplifier.getValue(DeviceParameter.Id.AmpNoiseReductionSensitivity).toZeroToTenDial(),
        amplifier.descriptor.model,
        amplifier.getValue(DeviceParameter.Id.Gain).toZeroToTenDial(),
        amplifier.getValue(DeviceParameter.Id.EqTreble).toZeroToTenDial(),
        amplifier.getValue(DeviceParameter.Id.EqMiddle).toZeroToTenDial(),
        amplifier.getValue(DeviceParameter.Id.EqBass).toZeroToTenDial(),
        amplifier.getValue(DeviceParameter.Id.AmpVolume).toZeroToTenDial(),
        amplifier.getValue(DeviceParameter.Id.AmpPresence).toZeroToTenDial(),
        amplifier.getValue(DeviceParameter.Id.Resonance).toZeroToTenDial(),
        amplifier.getValue(DeviceParameter.Id.AmpBrightCap),
        amplifier.getValue(DeviceParameter.Id.AmpLowCut),
        amplifier.getValue(DeviceParameter.Id.AmpMidBoost),
        amplifier.getValue(DeviceParameter.Id.AmpTubeBias),
        amplifier.getValue(DeviceParameter.Id.AmpClass),
        // defaults
        pedalOne.getValue(DeviceParameter.Id.PedalEnabled),
        Slot1PedalType.COMP,
        TwoByteDial(0u),
        0,
        0,
        0,
        0,
        0,
        pedalTwo.getValue(DeviceParameter.Id.PedalEnabled),
        Slot2PedalType.FLANGER,
        TwoByteDial(0u),
        0,
        0,
        0,
        0,
        0,
        reverbPedal.getValue(DeviceParameter.Id.PedalEnabled),
        ReverbPedalType.ROOM,
        ZeroToTenDial(0),
        ZeroToTenDial(0),
        0,
        ZeroToTenDial(0),
        ZeroToTenDial(0),
    )
        .withPedalOne(pedalOne)
        .withPedalTwo(pedalTwo)
        .withReverbPedal(reverbPedal)
}

private fun Program.withPedalOne(pedal: DeviceConfiguration<SlotOnePedalDescriptor>) = when(pedal.descriptor) {
    CompressorPedalDescriptor -> copy(
        pedal1Type = Slot1PedalType.COMP,
        pedal1Dial1 = pedal.getValue(DeviceParameter.Id.CompSensitivity).toZeroToTenDial().asTwoByte(),
        pedal1Dial2 = pedal.getValue(DeviceParameter.Id.PedalLevel).intValue.toByte(),
        pedal1Dial3 = pedal.getValue(DeviceParameter.Id.CompAttack).intValue.toByte(),
        pedal1Dial4 = pedal.getValue(DeviceParameter.Id.CompVoice).toProtocolDataModel(),
        pedal1Dial5 = 0,
        pedal1Dial6 = 0,
    )
    ChorusPedalDescriptor -> copy(
        pedal1Type = Slot1PedalType.CHORUS,
        pedal1Dial1 = pedal.getValue(DeviceParameter.Id.ModulationSpeed).toTwoByteDial(),
        pedal1Dial2 = pedal.getValue(DeviceParameter.Id.ModulationDepth).intValue.toByte(),
        pedal1Dial3 = pedal.getValue(DeviceParameter.Id.ModulationManual).intValue.toByte(),
        pedal1Dial4 = pedal.getValue(DeviceParameter.Id.PedalMix).intValue.toByte(),
        pedal1Dial5 = pedal.getValue(DeviceParameter.Id.EqLowCut).toByte(),
        pedal1Dial6 = pedal.getValue(DeviceParameter.Id.EqHighCut).toByte(),
    )
    is OverdrivePedalDescriptor -> copy(
        pedal1Type = when(pedal.descriptor) {
            TubeOdDescriptor -> Slot1PedalType.OVERDRIVE
            GoldDriveDescriptor -> Slot1PedalType.GOLD_DRIVE
            TrebleBoostDescriptor -> Slot1PedalType.TREBLE_BOOST
            RcTurboDescriptor -> Slot1PedalType.RC_TURBO
            OrangeDistDescriptor -> Slot1PedalType.ORANGE_DIST
            FatDistDescriptor -> Slot1PedalType.FAT_DIST
            BritLeadDescriptor -> Slot1PedalType.BRIT_LEAD
            FuzzDescriptor -> Slot1PedalType.FUZZ
        },
        pedal1Dial1 = pedal.getValue(DeviceParameter.Id.OverdriveDrive).toTwoByteDial(),
        pedal1Dial2 = pedal.getValue(DeviceParameter.Id.EqTone).intValue.toByte(),
        pedal1Dial3 = pedal.getValue(DeviceParameter.Id.PedalLevel).intValue.toByte(),
        pedal1Dial4 = pedal.getValue(DeviceParameter.Id.EqTreble).intValue.toByte(),
        pedal1Dial5 = pedal.getValue(DeviceParameter.Id.EqMiddle).intValue.toByte(),
        pedal1Dial6 = pedal.getValue(DeviceParameter.Id.EqBass).intValue.toByte(),
    )
}

private fun Program.withPedalTwo(pedal: DeviceConfiguration<SlotTwoPedalDescriptor>): Program = when(pedal.descriptor) {
    FlangerPedalDescriptor -> copy(
        pedal2Type = Slot2PedalType.FLANGER,
        pedal2Dial1 = pedal.getValue(DeviceParameter.Id.ModulationSpeed).toTwoByteDial(),
        pedal2Dial2 = pedal.getValue(DeviceParameter.Id.ModulationDepth).intValue.toByte(),
        pedal2Dial3 = pedal.getValue(DeviceParameter.Id.ModulationManual).intValue.toByte(),
        pedal2Dial4 = pedal.getValue(DeviceParameter.Id.EqLowCut).toByte(),
        pedal2Dial5 = pedal.getValue(DeviceParameter.Id.EqHighCut).toByte(),
        pedal2Dial6 = pedal.getValue(DeviceParameter.Id.Resonance).intValue.toByte(),
    )
    is PhaserPedalDescriptor -> copy(
        pedal2Type = when (pedal.descriptor) {
            BlkPhaserDescriptor -> Slot2PedalType.BLK_PHASER
            OrgPhaserOneDescriptor -> Slot2PedalType.ORG_PHASER_1
            OrgPhaserTwoDescriptor -> Slot2PedalType.ORG_PHASER_2
        },
        pedal2Dial1 = pedal.getValue(DeviceParameter.Id.ModulationSpeed).toTwoByteDial(),
        pedal2Dial2 = pedal.getValue(DeviceParameter.Id.Resonance).intValue.toByte(),
        pedal2Dial3 = pedal.getValue(DeviceParameter.Id.ModulationManual).intValue.toByte(),
        pedal2Dial4 = pedal.getValue(DeviceParameter.Id.ModulationDepth).intValue.toByte(),
        pedal2Dial5 = 0,
        pedal2Dial6 = 0,
    )
    TremoloPedalDescriptor -> copy(
        pedal2Type = Slot2PedalType.TREMOLO,
        pedal2Dial1 = pedal.getValue(DeviceParameter.Id.ModulationSpeed).toTwoByteDial(),
        pedal2Dial2 = pedal.getValue(DeviceParameter.Id.ModulationDepth).intValue.toByte(),
        pedal2Dial3 = pedal.getValue(DeviceParameter.Id.TremoloDuty).intValue.toByte(),
        pedal2Dial4 = pedal.getValue(DeviceParameter.Id.TremoloShape).intValue.toByte(),
        pedal2Dial5 = pedal.getValue(DeviceParameter.Id.PedalLevel).intValue.toByte(),
        pedal2Dial6 = 0,
    )
    is DelayPedalDescriptor -> copy(
        pedal2Type = when(pedal.descriptor) {
            TapeEchoDescriptor -> Slot2PedalType.TAPE_ECHO
            AnalogDelayDescriptor -> Slot2PedalType.ANALOG_DELAY
        },
        pedal2Dial1 = pedal.getValue(DeviceParameter.Id.DelayTime).toTwoByteDial(),
        pedal2Dial2 = pedal.getValue(DeviceParameter.Id.PedalLevel).intValue.toByte(),
        pedal2Dial3 = pedal.getValue(DeviceParameter.Id.DelayFeedback).intValue.toByte(),
        pedal2Dial4 = pedal.getValue(DeviceParameter.Id.EqTone).intValue.toByte(),
        pedal2Dial5 = pedal.getValue(DeviceParameter.Id.DelayModulationSpeed).intValue.toByte(),
        pedal2Dial6 = pedal.getValue(DeviceParameter.Id.ModulationDepth).intValue.toByte(),
    )
}

private fun Program.withReverbPedal(pedal: DeviceConfiguration<ReverbPedalDescriptor>): Program = copy(
    reverbPedalType = when(pedal.descriptor) {
        RoomReverbPedalDescriptor -> ReverbPedalType.ROOM
        SpringReverbPedalDescriptor -> ReverbPedalType.SPRING
        HallReverbPedalDescriptor -> ReverbPedalType.HALL
        PlateReverbPedalDescriptor -> ReverbPedalType.PLATE
    },
    reverbPedalDial1 = pedal.getValue(DeviceParameter.Id.PedalMix).toZeroToTenDial(),
    reverbPedalDial2 = pedal.getValue(DeviceParameter.Id.ReverbTime).toZeroToTenDial(),
    reverbPedalDial3 = pedal.getValue(DeviceParameter.Id.ReverbPreDelay).milliseconds.toByte(),
    reverbPedalDial4 = pedal.getValue(DeviceParameter.Id.ReverbLowDamp).toZeroToTenDial(),
    reverbPedalDial5 = pedal.getValue(DeviceParameter.Id.ReverbHighDamp).toZeroToTenDial(),
)