package com.github.tmarsteel.voxamplibrarian.appmodel.hardware_integration

import com.github.tmarsteel.voxamplibrarian.VOX_AMP_MIDI_DEVICE
import com.github.tmarsteel.voxamplibrarian.appmodel.*
import com.github.tmarsteel.voxamplibrarian.appmodel.ParameterValue.Companion.withValue
import com.github.tmarsteel.voxamplibrarian.hex
import com.github.tmarsteel.voxamplibrarian.protocol.*
import com.github.tmarsteel.voxamplibrarian.protocol.message.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.runningFold

class VoxVtxAmpConnection(
    midiDevice: MidiDevice,
) {
    private val client = VoxVtxAmplifierClient(midiDevice, listener = this::onMessage)

    private val messagesToHost = Channel<MessageToHost>(Channel.BUFFERED)
    val ampState: Flow<VtxAmpState> = flow {
        val state = fetchCurrentState()
        emit(state)
        messagesToHost.consumeAsFlow()
            .runningFold(state) { previousState, message ->
                try {
                    previousState.plus(message)
                }
                catch (ex: DifferentialUpdateNotSupportedException) {
                    fetchCurrentState()
                }
            }
            .collect {
                emit(it)
            }
    }

    private suspend fun onMessage(message: MessageToHost) {
        messagesToHost.send(message)
    }

    private fun VtxAmpState.plus(diff: MessageToHost): VtxAmpState {
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

    private suspend fun fetchCurrentState(): VtxAmpState {
        val currentModeResponse = client.exchange(RequestCurrentModeMessage())
        val currentConfig = client.exchange(RequestCurrentProgramMessage()).program.toUiDataModel()
        return when(currentModeResponse.mode) {
            CurrentModeResponse.Mode.PROGRAM_SLOT -> {
                VtxAmpState.ProgramSlotSelected(currentModeResponse.slot!!, currentConfig)
            }
            CurrentModeResponse.Mode.PRESET -> {
                VtxAmpState.PresetMode(currentModeResponse.presetIdentifier!!, currentConfig)
            }
            CurrentModeResponse.Mode.MANUAL -> {
                VtxAmpState.ManualMode(currentConfig)
            }
        }
    }

    fun close() {

    }

    companion object {
        val VOX_AMP: Flow<VoxVtxAmpConnection?> = VOX_AMP_MIDI_DEVICE
            .runningFold<MidiDevice?, VoxVtxAmpConnection?>(null) { currentConnection, midiDevice ->
                currentConnection?.close()

                if (midiDevice == null) {
                    return@runningFold null
                }

                VoxVtxAmpConnection(midiDevice)
            }
    }
}

private class DifferentialUpdateNotSupportedException : RuntimeException()

private fun ZeroToTenDial.toUiDataModel() = UnitlessSingleDecimalPrecision(value.toInt())
private fun Byte.toUiZeroToTenDial() = UnitlessSingleDecimalPrecision(toInt())
private fun TwoByteDial.toFrequency() = Frequency(this.semanticValue.toInt())
private fun TwoByteDial.toDuration() = Duration(this.semanticValue.toInt())
private fun Byte.toDuration() = Duration(this.toInt())

private fun Program.toUiDataModel(): SimulationConfiguration {
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

private val AmpModel.descriptor: AmplifierDescriptor get() = when(this) {
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

private val Program.slotOnePedal: DeviceConfiguration<SlotOnePedalDescriptor> get() = when(pedal1Type) {
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

private val Program.slotTwoPedal: DeviceConfiguration<SlotTwoPedalDescriptor> get() = when(pedal2Type) {
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

private val Program.reverbPedal: DeviceConfiguration<ReverbPedalDescriptor> get() = DeviceConfiguration(
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

private fun <T : AmplifierDescriptor> DeviceConfiguration<T>.plus(diff: AmpDialTurnedMessage): DeviceConfiguration<T> {
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

private fun Int.asCompressorVoice(): CompressorPedalDescriptor.Voice = when (this) {
    0x00 -> CompressorPedalDescriptor.Voice.ONE
    0x01 -> CompressorPedalDescriptor.Voice.TWO
    0x02 -> CompressorPedalDescriptor.Voice.THREE
    else -> error("Unknown compressor pedal voice $this")
}

private fun <T : SlotOnePedalDescriptor> DeviceConfiguration<T>.plus(diff: EffectDialTurnedMessage): DeviceConfiguration<T> {
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

private fun <T : AmplifierDescriptor> DeviceConfiguration<T>.plus(diff: NoiseReductionSensitivityChangedMessage): DeviceConfiguration<T> {
    return withValue(DeviceParameter.Id.AmpNoiseReductionSensitivity, diff.sensitivity.toUiDataModel())
}

private fun <T : SlotTwoPedalDescriptor> DeviceConfiguration<T>.plus(diff: EffectDialTurnedMessage): DeviceConfiguration<T> {
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

private fun <T : ReverbPedalDescriptor> DeviceConfiguration<T>.plus(diff: EffectDialTurnedMessage): DeviceConfiguration<T> {
    return when(diff.dialIndex.toInt()) {
        0x00 -> withValue(DeviceParameter.Id.PedalMix, diff.value.asZeroToTen().toUiDataModel())
        0x01 -> withValue(DeviceParameter.Id.ReverbTime, diff.value.asZeroToTen().toUiDataModel())
        0x02 -> withValue(DeviceParameter.Id.ReverbPreDelay, diff.value.toDuration())
        0x03 -> withValue(DeviceParameter.Id.ReverbLowDamp, diff.value.asZeroToTen().toUiDataModel())
        0x04 -> withValue(DeviceParameter.Id.ReverbHighDamp, diff.value.asZeroToTen().toUiDataModel())
        else -> error("Unknown reverb pedal dial ${diff.dialIndex.hex()}")
    }
}

private val Slot1PedalType.descriptor: SlotOnePedalDescriptor get() = when(this) {
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

private val Slot2PedalType.descriptor: SlotTwoPedalDescriptor get() = when(this) {
    Slot2PedalType.FLANGER -> FlangerPedalDescriptor
    Slot2PedalType.BLK_PHASER -> BlkPhaserDescriptor
    Slot2PedalType.ORG_PHASER_1 -> OrgPhaserOneDescriptor
    Slot2PedalType.ORG_PHASER_2 -> OrgPhaserTwoDescriptor
    Slot2PedalType.TREMOLO -> TremoloPedalDescriptor
    Slot2PedalType.TAPE_ECHO -> TapeEchoDescriptor
    Slot2PedalType.ANALOG_DELAY -> AnalogDelayDescriptor
}

private val ReverbPedalType.descriptor: ReverbPedalDescriptor get() = when(this) {
    ReverbPedalType.ROOM -> RoomReverbPedalDescriptor
    ReverbPedalType.SPRING -> SpringReverbPedalDescriptor
    ReverbPedalType.HALL -> HallReverbPedalDescriptor
    ReverbPedalType.PLATE -> PlateReverbPedalDescriptor
}

// ------------------------------------------------------------------------------------------------

private fun UnitlessSingleDecimalPrecision.toZeroToTenDial() = ZeroToTenDial(intValue.toByte())
private val AmplifierDescriptor.model: AmpModel get() = when(this) {
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

private fun SimulationConfiguration.toProtocolDataModel(): Program {
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