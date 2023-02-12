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
import com.github.tmarsteel.voxamplibrarian.appmodel.DelayPedalDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.DeluxeClNormalAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.DeluxeClVibratoAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceConfiguration
import com.github.tmarsteel.voxamplibrarian.appmodel.DeviceParameter
import com.github.tmarsteel.voxamplibrarian.appmodel.DoubleRecAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.Duration
import com.github.tmarsteel.voxamplibrarian.appmodel.EruptThreeChannelThreeAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.EruptThreeChannelTwoAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.FatDistDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.FlangerPedalDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.Frequency
import com.github.tmarsteel.voxamplibrarian.appmodel.FuzzDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.GoldDriveDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.HallReverbPedalDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.OrangeDistDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.OrgPhaserOneDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.OrgPhaserTwoDescriptor
import com.github.tmarsteel.voxamplibrarian.appmodel.OriginalCleanAmplifier
import com.github.tmarsteel.voxamplibrarian.appmodel.ParameterValue.Companion.withValue
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
import com.github.tmarsteel.voxamplibrarian.appmodel.VtxAmpState
import com.github.tmarsteel.voxamplibrarian.hex
import com.github.tmarsteel.voxamplibrarian.protocol.AmpClass
import com.github.tmarsteel.voxamplibrarian.protocol.AmpModel
import com.github.tmarsteel.voxamplibrarian.protocol.PedalSlot
import com.github.tmarsteel.voxamplibrarian.protocol.Program
import com.github.tmarsteel.voxamplibrarian.protocol.ReverbPedalType
import com.github.tmarsteel.voxamplibrarian.protocol.Slot1PedalType
import com.github.tmarsteel.voxamplibrarian.protocol.Slot2PedalType
import com.github.tmarsteel.voxamplibrarian.protocol.TubeBias
import com.github.tmarsteel.voxamplibrarian.protocol.TwoByteDial
import com.github.tmarsteel.voxamplibrarian.protocol.ZeroToTenDial
import com.github.tmarsteel.voxamplibrarian.protocol.message.AmpDialTurnedMessage
import com.github.tmarsteel.voxamplibrarian.protocol.message.EffectDialTurnedMessage
import com.github.tmarsteel.voxamplibrarian.protocol.message.EffectPedalTypeChangedMessage
import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageToHost
import com.github.tmarsteel.voxamplibrarian.protocol.message.NoiseReductionSensitivityChangedMessage
import com.github.tmarsteel.voxamplibrarian.protocol.message.PedalActiveStateChangedMessage
import com.github.tmarsteel.voxamplibrarian.protocol.message.ProgramSlotChangedMessage
import com.github.tmarsteel.voxamplibrarian.protocol.message.SimulatedAmpModelChangedMessage

internal fun ZeroToTenDial.toUiDataModel() = UnitlessSingleDecimalPrecision(value.toInt())
internal fun Byte.toUiZeroToTenDial() = UnitlessSingleDecimalPrecision(toInt())
internal fun TwoByteDial.toFrequency() = Frequency(this.semanticValue.toInt())
internal fun TwoByteDial.toDuration() = Duration(this.semanticValue.toInt())
internal fun Byte.toDuration() = Duration(this.toInt())

internal fun Program.toUiDataModel(): SimulationConfiguration {
    return SimulationConfiguration(
        programName = programName.name,
        amplifier = DeviceConfiguration(
            ampModel.descriptor,
            listOf(
                DeviceParameter.Id.Gain withValue gain.toUiDataModel(),
                DeviceParameter.Id.EqBass withValue bass.toUiDataModel(),
                DeviceParameter.Id.EqMiddle withValue middle.toUiDataModel(),
                DeviceParameter.Id.EqTreble withValue treble.toUiDataModel(),
                DeviceParameter.Id.AmpVolume withValue volume.toUiDataModel(),
                DeviceParameter.Id.Resonance withValue resonance.toUiDataModel(),
                DeviceParameter.Id.AmpNoiseReductionSensitivity withValue noiseReductionSensitivity.toUiDataModel(),
                DeviceParameter.Id.AmpLowCut withValue lowCut,
                DeviceParameter.Id.AmpMidBoost withValue midBoost,
                DeviceParameter.Id.AmpBrightCap withValue brightCap,
                DeviceParameter.Id.AmpTubeBias withValue tubeBias,
                DeviceParameter.Id.AmpClass withValue ampClass,
                DeviceParameter.Id.AmpTone withValue presence.toUiDataModel(),
                DeviceParameter.Id.AmpPresence withValue presence.toUiDataModel(),
            ),
        ),
        pedalOne = slotOnePedal,
        pedalTwo = slotTwoPedal,
        reverbPedal = reverbPedal,
    )
}

internal val AmpModel.descriptor: AmplifierDescriptor
    get() = when(this) {
    AmpModel.DELUXE_CL_VIBRATO -> DeluxeClVibratoAmplifier
    AmpModel.DELUXE_CL_NORMAL -> DeluxeClNormalAmplifier
    AmpModel.TWEED_410_BRIGHT -> Tweed4X10BrightAmplifier
    AmpModel.TWEED_410_NORMAL -> Tweed4X10NormalAmplifier
    AmpModel.BOUTIQUE_CL -> BoutiqueClAmplifier
    AmpModel.BOUTIQUE_OD -> BoutiqueOdAmplifier
    AmpModel.VOX_AC30 -> VoxAc30Amplifier
    AmpModel.VOX_AC30TB -> VoxAc30TbAmplifier
    AmpModel.BRIT_1959_TREBLE -> Brit1959TrebleAmplifier
    AmpModel.BRIT_1959_NORMAL -> Brit1959NormalAmplifier
    AmpModel.BRIT_800 -> Brit800Amplifier
    AmpModel.BRIT_VM -> BritVmAmplifier
    AmpModel.SL_OD -> SlOdAmplifier
    AmpModel.DOUBLE_REC -> DoubleRecAmplifier
    AmpModel.CALI_ELATION -> CaliElationAmplifier
    AmpModel.ERUPT_III_CH2 -> EruptThreeChannelTwoAmplifier
    AmpModel.ERUPT_III_CH3 -> EruptThreeChannelThreeAmplifier
    AmpModel.BOUTIQUE_METAL -> BoutiqueMetalAmplifier
    AmpModel.BRIT_OR_MKII -> BritOrMkTwoAmplifier
    AmpModel.ORIGINAL_CL -> OriginalCleanAmplifier
}

internal val Program.slotOnePedal: DeviceConfiguration<SlotOnePedalDescriptor>
    get() = when(pedal1Type) {
    Slot1PedalType.COMP -> DeviceConfiguration(
        CompressorPedalDescriptor,
        listOf(
            DeviceParameter.Id.PedalEnabled withValue pedal1Enabled,
            DeviceParameter.Id.CompSensitivity withValue pedal1Dial1.asZeroToTen().toUiDataModel(),
            DeviceParameter.Id.PedalLevel withValue pedal1Dial2.toUiZeroToTenDial(),
            DeviceParameter.Id.CompAttack withValue pedal1Dial3.toUiZeroToTenDial(),
            DeviceParameter.Id.CompVoice withValue pedal1Dial4.toInt().asCompressorVoice(),
        )
    )
    Slot1PedalType.CHORUS -> DeviceConfiguration(
        ChorusPedalDescriptor,
        listOf(
            DeviceParameter.Id.PedalEnabled withValue pedal1Enabled,
            DeviceParameter.Id.ModulationSpeed withValue pedal1Dial1.toFrequency(),
            DeviceParameter.Id.ModulationDepth withValue pedal1Dial2.toUiZeroToTenDial(),
            DeviceParameter.Id.ModulationManual withValue pedal1Dial3.toUiZeroToTenDial(),
            DeviceParameter.Id.PedalMix withValue pedal1Dial4.toUiZeroToTenDial(),
            DeviceParameter.Id.EqLowCut withValue (pedal1Dial5.toInt() == 1),
            DeviceParameter.Id.EqHighCut withValue (pedal1Dial6.toInt() == 1),
        )
    )
    else -> {
        DeviceConfiguration(
            when (pedal1Type) {
                Slot1PedalType.OVERDRIVE -> TubeOdDescriptor
                Slot1PedalType.GOLD_DRIVE -> GoldDriveDescriptor
                Slot1PedalType.TREBLE_BOOST -> TrebleBoostDescriptor
                Slot1PedalType.RC_TURBO -> RcTurboDescriptor
                Slot1PedalType.ORANGE_DIST -> OrangeDistDescriptor
                Slot1PedalType.FAT_DIST -> FatDistDescriptor
                Slot1PedalType.BRIT_LEAD -> BritLeadDescriptor
                Slot1PedalType.FUZZ -> FuzzDescriptor
                else -> error("unreachable")
            },
            listOf(
                DeviceParameter.Id.PedalEnabled withValue pedal1Enabled,
                DeviceParameter.Id.OverdriveDrive withValue pedal1Dial1.asZeroToTen().toUiDataModel(),
                DeviceParameter.Id.EqTone withValue pedal1Dial2.toUiZeroToTenDial(),
                DeviceParameter.Id.PedalLevel withValue pedal1Dial3.toUiZeroToTenDial(),
                DeviceParameter.Id.EqTreble withValue pedal1Dial4.toUiZeroToTenDial(),
                DeviceParameter.Id.EqMiddle withValue pedal1Dial5.toUiZeroToTenDial(),
                DeviceParameter.Id.EqBass withValue pedal1Dial6.toUiZeroToTenDial(),
            )
        )
    }
}

internal val Program.slotTwoPedal: DeviceConfiguration<SlotTwoPedalDescriptor>
    get() = when(pedal2Type) {
    Slot2PedalType.FLANGER -> DeviceConfiguration(
        FlangerPedalDescriptor,
        listOf(
            DeviceParameter.Id.PedalEnabled withValue pedal2Enabled,
            DeviceParameter.Id.ModulationSpeed withValue pedal2Dial1.toFrequency(),
            DeviceParameter.Id.ModulationDepth withValue pedal2Dial2.toUiZeroToTenDial(),
            DeviceParameter.Id.ModulationManual withValue pedal2Dial3.toUiZeroToTenDial(),
            DeviceParameter.Id.EqLowCut withValue (pedal2Dial4.toInt() == 1),
            DeviceParameter.Id.EqHighCut withValue (pedal2Dial5.toInt() == 1),
            DeviceParameter.Id.Resonance withValue pedal2Dial6.toUiZeroToTenDial(),
        ),
    )
    Slot2PedalType.BLK_PHASER,
    Slot2PedalType.ORG_PHASER_1,
    Slot2PedalType.ORG_PHASER_2 -> DeviceConfiguration(
        when(pedal2Type) {
            Slot2PedalType.BLK_PHASER -> BlkPhaserDescriptor
            Slot2PedalType.ORG_PHASER_1 -> OrgPhaserOneDescriptor
            Slot2PedalType.ORG_PHASER_2 -> OrgPhaserTwoDescriptor
            else -> error("unreachable")
        },
        listOf(
            DeviceParameter.Id.PedalEnabled withValue pedal2Enabled,
            DeviceParameter.Id.ModulationSpeed withValue pedal2Dial1.toFrequency(),
            DeviceParameter.Id.Resonance withValue pedal2Dial2.toUiZeroToTenDial(),
            DeviceParameter.Id.ModulationManual withValue pedal2Dial3.toUiZeroToTenDial(),
            DeviceParameter.Id.ModulationDepth withValue pedal2Dial4.toUiZeroToTenDial(),
        ),
    )
    Slot2PedalType.TREMOLO -> DeviceConfiguration(
        TremoloPedalDescriptor,
        listOf(
            DeviceParameter.Id.PedalEnabled withValue pedal2Enabled,
            DeviceParameter.Id.ModulationSpeed withValue pedal2Dial1.toFrequency(),
            DeviceParameter.Id.ModulationDepth withValue pedal2Dial2.toUiZeroToTenDial(),
            DeviceParameter.Id.TremoloDuty withValue pedal2Dial3.toUiZeroToTenDial(),
            DeviceParameter.Id.TremoloShape withValue pedal2Dial4.toUiZeroToTenDial(),
            DeviceParameter.Id.PedalLevel withValue pedal2Dial5.toUiZeroToTenDial(),
        )
    )
    else -> DeviceConfiguration(
        when (pedal2Type) {
            Slot2PedalType.TAPE_ECHO -> TapeEchoDescriptor
            Slot2PedalType.ANALOG_DELAY -> AnalogDelayDescriptor
            else -> error("unreachable")
        },
        listOf(
            DeviceParameter.Id.PedalEnabled withValue pedal2Enabled,
            DeviceParameter.Id.DelayTime withValue pedal2Dial1.toDuration(),
            DeviceParameter.Id.PedalLevel withValue pedal2Dial2.toUiZeroToTenDial(),
            DeviceParameter.Id.DelayFeedback withValue pedal2Dial3.toUiZeroToTenDial(),
            DeviceParameter.Id.EqTone withValue pedal2Dial4.toUiZeroToTenDial(),
            DeviceParameter.Id.DelayModulationSpeed withValue pedal2Dial5.toUiZeroToTenDial(),
            DeviceParameter.Id.ModulationDepth withValue pedal2Dial6.toUiZeroToTenDial(),
        )
    )
}

internal val Program.reverbPedal: DeviceConfiguration<ReverbPedalDescriptor>
    get() = DeviceConfiguration(
    when(reverbPedalType) {
        ReverbPedalType.ROOM -> RoomReverbPedalDescriptor
        ReverbPedalType.SPRING -> SpringReverbPedalDescriptor
        ReverbPedalType.HALL -> HallReverbPedalDescriptor
        ReverbPedalType.PLATE -> PlateReverbPedalDescriptor
    },
    listOf(
        DeviceParameter.Id.PedalEnabled withValue reverbPedalEnabled,
        DeviceParameter.Id.PedalMix withValue reverbPedalDial1.toUiDataModel(),
        DeviceParameter.Id.ReverbTime withValue reverbPedalDial2.toUiDataModel(),
        DeviceParameter.Id.ReverbPreDelay withValue reverbPedalDial3.toDuration(),
        DeviceParameter.Id.ReverbLowDamp withValue reverbPedalDial4.toUiDataModel(),
        DeviceParameter.Id.ReverbHighDamp withValue reverbPedalDial5.toUiDataModel(),
    )
)

internal fun <T : AmplifierDescriptor> DeviceConfiguration<T>.plus(diff: AmpDialTurnedMessage): DeviceConfiguration<T> {
    return when (diff.dial.toInt()) {
        0x00 -> withValue(DeviceParameter.Id.Gain, diff.value.asZeroToTen().toUiDataModel())
        0x01 -> withValue(DeviceParameter.Id.EqTreble, diff.value.asZeroToTen().toUiDataModel())
        0x02 -> withValue(DeviceParameter.Id.EqMiddle, diff.value.asZeroToTen().toUiDataModel())
        0x03 -> withValue(DeviceParameter.Id.EqBass, diff.value.asZeroToTen().toUiDataModel())
        0x04 -> withValue(DeviceParameter.Id.AmpVolume, diff.value.asZeroToTen().toUiDataModel())
        0x05 -> withValue(
            if (descriptor.presenceIsCalledTone) DeviceParameter.Id.AmpTone else DeviceParameter.Id.AmpPresence,
            diff.value.asZeroToTen().toUiDataModel(),
        )
        0x06 -> withValue(DeviceParameter.Id.Resonance, diff.value.asZeroToTen().toUiDataModel(),)
        0x07 -> withValue(DeviceParameter.Id.AmpBrightCap, diff.value.semanticValue.toInt() == 1)
        0x08 -> withValue(DeviceParameter.Id.AmpLowCut, diff.value.semanticValue.toInt() == 1)
        0x09 -> withValue(DeviceParameter.Id.AmpMidBoost, diff.value.semanticValue.toInt() == 1)
        0x0A -> withValue(
            DeviceParameter.Id.AmpTubeBias,
            when (diff.value.semanticValue.toInt()) {
                0 -> TubeBias.OFF
                1 -> TubeBias.COLD
                2 -> TubeBias.HOT
                else -> error("Unrecognized value for tube bias: ${diff.value}")
            }
        )
        0x0B -> withValue(
            DeviceParameter.Id.AmpClass,
            when (diff.value.semanticValue.toInt()) {
                0 -> AmpClass.A
                1 -> AmpClass.AB
                else -> error("Unrecognized value for amp class: ${diff.value}")
            }
        )
        else -> {
            console.error("Unimplemented amp dial ${diff.dial}")
            this
        }
    }
}

internal fun Int.asCompressorVoice(): CompressorPedalDescriptor.Voice = when (this) {
    0x00 -> CompressorPedalDescriptor.Voice.ONE
    0x01 -> CompressorPedalDescriptor.Voice.TWO
    0x02 -> CompressorPedalDescriptor.Voice.THREE
    else -> error("Unknown compressor pedal voice $this")
}

internal fun <T : SlotOnePedalDescriptor> DeviceConfiguration<T>.plus(diff: EffectDialTurnedMessage): DeviceConfiguration<T> {
    return when (descriptor) {
        is CompressorPedalDescriptor -> when (diff.dialIndex.toInt()) {
            0x00 -> withValue(DeviceParameter.Id.CompSensitivity, diff.value.asZeroToTen().toUiDataModel())
            0x01 -> withValue(DeviceParameter.Id.PedalLevel, diff.value.asZeroToTen().toUiDataModel())
            0x02 -> withValue(DeviceParameter.Id.CompAttack, diff.value.asZeroToTen().toUiDataModel())
            0x03 -> withValue(DeviceParameter.Id.CompVoice, diff.value.semanticValue.toInt().asCompressorVoice())
            else -> error("Unknown compressor dial ${diff.dialIndex}")
        }
        is ChorusPedalDescriptor -> when (diff.dialIndex.toInt()) {
            0x00 -> withValue(DeviceParameter.Id.ModulationSpeed, diff.value.toFrequency())
            0x01 -> withValue(DeviceParameter.Id.ModulationDepth, diff.value.asZeroToTen().toUiDataModel())
            0x02 -> withValue(DeviceParameter.Id.ModulationManual, diff.value.asZeroToTen().toUiDataModel())
            0x03 -> withValue(DeviceParameter.Id.PedalMix, diff.value.asZeroToTen().toUiDataModel())
            0x04 -> withValue(DeviceParameter.Id.EqLowCut, diff.value.semanticValue.toInt() == 1)
            0x05 -> withValue(DeviceParameter.Id.EqHighCut, diff.value.semanticValue.toInt() == 1)
            else -> error("Unknown chorus pedal dial ${diff.dialIndex}")
        }
        else -> when (diff.dialIndex.toInt()) {
            0x00 -> withValue(DeviceParameter.Id.OverdriveDrive, diff.value.asZeroToTen().toUiDataModel())
            0x01 -> withValue(DeviceParameter.Id.EqTone, diff.value.asZeroToTen().toUiDataModel())
            0x02 -> withValue(DeviceParameter.Id.PedalLevel, diff.value.asZeroToTen().toUiDataModel())
            0x03 -> withValue(DeviceParameter.Id.EqTreble, diff.value.asZeroToTen().toUiDataModel())
            0x04 -> withValue(DeviceParameter.Id.EqMiddle, diff.value.asZeroToTen().toUiDataModel())
            0x05 -> withValue(DeviceParameter.Id.EqBass, diff.value.asZeroToTen().toUiDataModel())
            else -> error("Unknown slot 1 pedal (${descriptor::class.simpleName}) dial ${diff.dialIndex}")
        }
    }
}

internal fun <T : AmplifierDescriptor> DeviceConfiguration<T>.plus(diff: NoiseReductionSensitivityChangedMessage): DeviceConfiguration<T> {
    return withValue(DeviceParameter.Id.AmpNoiseReductionSensitivity, diff.sensitivity.toUiDataModel())
}

internal fun <T : SlotTwoPedalDescriptor> DeviceConfiguration<T>.plus(diff: EffectDialTurnedMessage): DeviceConfiguration<T> {
    return when (descriptor) {
        is FlangerPedalDescriptor -> when (diff.dialIndex.toInt()) {
            0x00 -> withValue(DeviceParameter.Id.ModulationSpeed, diff.value.toFrequency())
            0x01 -> withValue(DeviceParameter.Id.ModulationDepth, diff.value.asZeroToTen().toUiDataModel())
            0x02 -> withValue(DeviceParameter.Id.ModulationManual, diff.value.asZeroToTen().toUiDataModel())
            0x03 -> withValue(DeviceParameter.Id.EqLowCut, diff.value.semanticValue.toInt() == 1)
            0x04 -> withValue(DeviceParameter.Id.EqHighCut, diff.value.semanticValue.toInt() == 1)
            0x05 -> withValue(DeviceParameter.Id.Resonance, diff.value.asZeroToTen().toUiDataModel())
            else -> error("Unknown flanger pedal dial ${diff.dialIndex.hex()}")
        }
        is BlkPhaserDescriptor,
        is OrgPhaserOneDescriptor,
        is OrgPhaserTwoDescriptor -> when (diff.dialIndex.toInt()) {
            0x00 -> withValue(DeviceParameter.Id.ModulationSpeed, diff.value.toFrequency())
            0x01 -> withValue(DeviceParameter.Id.Resonance, diff.value.asZeroToTen().toUiDataModel())
            0x02 -> withValue(DeviceParameter.Id.ModulationManual, diff.value.asZeroToTen().toUiDataModel())
            0x03 -> withValue(DeviceParameter.Id.ModulationDepth, diff.value.asZeroToTen().toUiDataModel())
            else -> error("Unknown modulation pedal (${descriptor::class.simpleName}) dial ${diff.dialIndex.hex()}")
        }
        is TremoloPedalDescriptor -> when (diff.dialIndex.toInt()) {
            0x00 -> withValue(DeviceParameter.Id.ModulationSpeed, diff.value.toFrequency())
            0x01 -> withValue(DeviceParameter.Id.Resonance, diff.value.asZeroToTen().toUiDataModel())
            0x02 -> withValue(DeviceParameter.Id.ModulationManual, diff.value.asZeroToTen().toUiDataModel())
            0x03 -> withValue(DeviceParameter.Id.ModulationDepth, diff.value.asZeroToTen().toUiDataModel())
            else -> error("Unknown tremolo pedal dial ${diff.dialIndex.hex()}")
        }
        is DelayPedalDescriptor -> when (diff.dialIndex.toInt()) {
            0x00 -> withValue(DeviceParameter.Id.DelayTime, diff.value.toDuration())
            0x01 -> withValue(DeviceParameter.Id.PedalLevel, diff.value.asZeroToTen().toUiDataModel())
            0x02 -> withValue(DeviceParameter.Id.DelayFeedback, diff.value.asZeroToTen().toUiDataModel())
            0x03 -> withValue(DeviceParameter.Id.EqTone, diff.value.asZeroToTen().toUiDataModel())
            0x04 -> withValue(DeviceParameter.Id.DelayModulationSpeed, diff.value.asZeroToTen().toUiDataModel())
            0x05 -> withValue(DeviceParameter.Id.ModulationDepth, diff.value.asZeroToTen().toUiDataModel())
            else -> error("Unknown delay pedal (${descriptor::class.simpleName}) dial ${diff.dialIndex.hex()}")
        }
        else -> error("Unknown slot two pedal ${descriptor::class.simpleName}")
    }
}

internal fun <T : ReverbPedalDescriptor> DeviceConfiguration<T>.plus(diff: EffectDialTurnedMessage): DeviceConfiguration<T> {
    return when(diff.dialIndex.toInt()) {
        0x00 -> withValue(DeviceParameter.Id.PedalMix, diff.value.asZeroToTen().toUiDataModel())
        0x01 -> withValue(DeviceParameter.Id.ReverbTime, diff.value.asZeroToTen().toUiDataModel())
        0x02 -> withValue(DeviceParameter.Id.ReverbPreDelay, diff.value.toDuration())
        0x03 -> withValue(DeviceParameter.Id.ReverbLowDamp, diff.value.asZeroToTen().toUiDataModel())
        0x04 -> withValue(DeviceParameter.Id.ReverbHighDamp, diff.value.asZeroToTen().toUiDataModel())
        else -> error("Unknown reverb pedal dial ${diff.dialIndex.hex()}")
    }
}

internal val Slot1PedalType.descriptor: SlotOnePedalDescriptor
    get() = when(this) {
    Slot1PedalType.COMP -> CompressorPedalDescriptor
    Slot1PedalType.CHORUS -> ChorusPedalDescriptor
    Slot1PedalType.OVERDRIVE -> TubeOdDescriptor
    Slot1PedalType.GOLD_DRIVE -> GoldDriveDescriptor
    Slot1PedalType.TREBLE_BOOST -> TrebleBoostDescriptor
    Slot1PedalType.RC_TURBO -> RcTurboDescriptor
    Slot1PedalType.ORANGE_DIST -> OrangeDistDescriptor
    Slot1PedalType.FAT_DIST -> FatDistDescriptor
    Slot1PedalType.BRIT_LEAD -> BritLeadDescriptor
    Slot1PedalType.FUZZ -> FuzzDescriptor
}

internal val Slot2PedalType.descriptor: SlotTwoPedalDescriptor
    get() = when(this) {
    Slot2PedalType.FLANGER -> FlangerPedalDescriptor
    Slot2PedalType.BLK_PHASER -> BlkPhaserDescriptor
    Slot2PedalType.ORG_PHASER_1 -> OrgPhaserOneDescriptor
    Slot2PedalType.ORG_PHASER_2 -> OrgPhaserTwoDescriptor
    Slot2PedalType.TREMOLO -> TremoloPedalDescriptor
    Slot2PedalType.TAPE_ECHO -> TapeEchoDescriptor
    Slot2PedalType.ANALOG_DELAY -> AnalogDelayDescriptor
}

internal val ReverbPedalType.descriptor: ReverbPedalDescriptor
    get() = when(this) {
    ReverbPedalType.ROOM -> RoomReverbPedalDescriptor
    ReverbPedalType.SPRING -> SpringReverbPedalDescriptor
    ReverbPedalType.HALL -> HallReverbPedalDescriptor
    ReverbPedalType.PLATE -> PlateReverbPedalDescriptor
}

fun VtxAmpState.plus(diff: MessageToHost): VtxAmpState {
    when (diff) {
        is AmpDialTurnedMessage -> {
            return this.withConfiguration(this.configuration.copy(
                amplifier = this.configuration.amplifier.plus(diff)
            ))
        }
        is EffectDialTurnedMessage -> {
            when (diff.pedalSlot) {
                PedalSlot.PEDAL1 -> {
                    return this.withConfiguration(this.configuration.copy(
                        pedalOne = this.configuration.pedalOne.plus(diff)
                    ))
                }
                PedalSlot.PEDAL2 -> {
                    return this.withConfiguration(this.configuration.copy(
                        pedalTwo = this.configuration.pedalTwo.plus(diff)
                    ))
                }
                PedalSlot.REVERB -> {
                    return this.withConfiguration(this.configuration.copy(
                        reverbPedal = this.configuration.reverbPedal.plus(diff)
                    ))
                }
            }
        }
        is EffectPedalTypeChangedMessage -> {
            return when (diff.type) {
                is Slot1PedalType -> this.withConfiguration(this.configuration.copy(
                    pedalOne = this.configuration.pedalOne.withDescriptor(diff.type.descriptor)
                ))
                is Slot2PedalType -> this.withConfiguration(this.configuration.copy(
                    pedalTwo = this.configuration.pedalTwo.withDescriptor(diff.type.descriptor)
                ))
                is ReverbPedalType -> this.withConfiguration(this.configuration.copy(
                    reverbPedal = this.configuration.reverbPedal.withDescriptor(diff.type.descriptor)
                ))
            }
        }
        is NoiseReductionSensitivityChangedMessage -> {
            return this.withConfiguration(this.configuration.copy(
                amplifier = this.configuration.amplifier.plus(diff)
            ))
        }
        is SimulatedAmpModelChangedMessage -> {
            return this.withConfiguration(this.configuration.copy(
                amplifier = this.configuration.amplifier.withDescriptor(diff.model.descriptor)
            ))
        }
        is PedalActiveStateChangedMessage -> {
            return when (diff.pedalSlot) {
                PedalSlot.PEDAL1 -> this.withConfiguration(this.configuration.copy(
                    pedalOne = this.configuration.pedalOne.withValue(
                        DeviceParameter.Id.PedalEnabled,
                        diff.enabled,
                    )
                ))
                PedalSlot.PEDAL2 -> this.withConfiguration(this.configuration.copy(
                    pedalTwo = this.configuration.pedalTwo.withValue(
                        DeviceParameter.Id.PedalEnabled,
                        diff.enabled,
                    )
                ))
                PedalSlot.REVERB -> this.withConfiguration(this.configuration.copy(
                    reverbPedal = this.configuration.reverbPedal.withValue(
                        DeviceParameter.Id.PedalEnabled,
                        diff.enabled,
                    )
                ))
            }
        }
        is ProgramSlotChangedMessage -> {
            throw DifferentialUpdateNotSupportedException()
        }
        else -> {
            console.error("Unimplemented message ${diff::class.simpleName}")
            return this
        }
    }
}