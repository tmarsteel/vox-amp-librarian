package com.github.tmarsteel.voxamplibrarian.appmodel.hardware_integration

import com.github.tmarsteel.voxamplibrarian.VOX_AMP_MIDI_DEVICE
import com.github.tmarsteel.voxamplibrarian.appmodel.*
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
        val currentConfig = client.exchange(RequestCurrentProgramMessage()).program.toConfiguration()
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

private fun Program.toConfiguration(): SimulationConfiguration {
    return SimulationConfiguration(
        programName = programName.name,
        amplifier = DeviceConfiguration(
            ampModel.descriptor,
            mapOf(
                DeviceParameter.Id.Gain to gain.value.toInt(),
                DeviceParameter.Id.EqBass to bass.value.toInt(),
                DeviceParameter.Id.EqMiddle to middle.value.toInt(),
                DeviceParameter.Id.EqTreble to treble.value.toInt(),
                DeviceParameter.Id.AmpVolume to volume.value.toInt(),
                DeviceParameter.Id.Resonance to resonance.value.toInt(),
                DeviceParameter.Id.AmpNoiseReductionSensitivity to noiseReductionSensitivity.value.toInt(),
                DeviceParameter.Id.AmpLowCut to lowCut,
                DeviceParameter.Id.AmpMidBoost to midBoost,
                DeviceParameter.Id.AmpBrightCap to brightCap,
                DeviceParameter.Id.AmpTubeBias to tubeBias,
                DeviceParameter.Id.AmpClass to ampClass,
                DeviceParameter.Id.AmpTone to presence.value.toInt(),
                DeviceParameter.Id.AmpPresence to presence.value.toInt(),
            ),
        ),
        pedalOne = slotOnePedal,
        pedalTwo = slotTwoPedal,
        reverbPedal = reverbPedal,
    )
}

private fun SimulationConfiguration.toProgram(): Program {
    TODO()
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
        mapOf(
            DeviceParameter.Id.PedalEnabled to pedal1Enabled,
            DeviceParameter.Id.CompSensitivity to pedal1Dial1.asZeroZoTen().value.toInt(),
            DeviceParameter.Id.PedalLevel to pedal1Dial2.toInt(),
            DeviceParameter.Id.CompAttack to pedal1Dial3.toInt(),
            DeviceParameter.Id.CompVoice to pedal1Dial4.toInt().asCompressorVoice(),
        )
    )
    Slot1PedalType.CHORUS -> DeviceConfiguration(
        ChorusPedalDescriptor,
        mapOf(
            DeviceParameter.Id.PedalEnabled to pedal1Enabled,
            DeviceParameter.Id.ModulationSpeed to pedal1Dial1.semanticValue.toInt(),
            DeviceParameter.Id.ModulationDepth to pedal1Dial2.toInt(),
            DeviceParameter.Id.ModulationManual to pedal1Dial3.toInt(),
            DeviceParameter.Id.PedalMix to pedal1Dial4.toInt(),
            DeviceParameter.Id.EqLowCut to (pedal1Dial5.toInt() == 1),
            DeviceParameter.Id.EqHighCut to (pedal1Dial6.toInt() == 1),
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
            mapOf(
                DeviceParameter.Id.PedalEnabled to pedal1Enabled,
                DeviceParameter.Id.OverdriveDrive to pedal1Dial1.asZeroZoTen().value.toInt(),
                DeviceParameter.Id.EqTone to pedal1Dial2.toInt(),
                DeviceParameter.Id.PedalLevel to pedal1Dial3.toInt(),
                DeviceParameter.Id.EqTreble to pedal1Dial4.toInt(),
                DeviceParameter.Id.EqMiddle to pedal1Dial5.toInt(),
                DeviceParameter.Id.EqBass to pedal1Dial6.toInt(),
            )
        )
    }
}

private val Program.slotTwoPedal: DeviceConfiguration<SlotTwoPedalDescriptor> get() = when(pedal2Type) {
    Slot2PedalType.FLANGER -> DeviceConfiguration(
        FlangerPedalDescriptor,
        mapOf(
            DeviceParameter.Id.PedalEnabled to pedal2Enabled,
            DeviceParameter.Id.ModulationSpeed to pedal2Dial1.semanticValue.toInt(),
            DeviceParameter.Id.ModulationDepth to pedal2Dial2.toInt(),
            DeviceParameter.Id.ModulationManual to pedal2Dial3.toInt(),
            DeviceParameter.Id.EqLowCut to (pedal2Dial4.toInt() == 1),
            DeviceParameter.Id.EqHighCut to (pedal2Dial5.toInt() == 1),
            DeviceParameter.Id.Resonance to pedal2Dial6.toInt(),
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
        mapOf(
            DeviceParameter.Id.PedalEnabled to pedal2Enabled,
            DeviceParameter.Id.ModulationSpeed to pedal2Dial1.semanticValue.toInt(),
            DeviceParameter.Id.Resonance to pedal2Dial2.toInt(),
            DeviceParameter.Id.ModulationManual to pedal2Dial3.toInt(),
            DeviceParameter.Id.ModulationDepth to pedal2Dial4.toInt(),
        ),
    )
    Slot2PedalType.TREMOLO -> DeviceConfiguration(
        TremoloPedalDescriptor,
        mapOf(
            DeviceParameter.Id.PedalEnabled to pedal2Enabled,
            DeviceParameter.Id.ModulationSpeed to pedal2Dial1.semanticValue.toInt(),
            DeviceParameter.Id.ModulationDepth to pedal2Dial2.toInt(),
            DeviceParameter.Id.TremoloDuty to pedal2Dial3.toInt(),
            DeviceParameter.Id.TremoloShape to pedal2Dial4.toInt(),
            DeviceParameter.Id.PedalLevel to pedal2Dial5.toInt(),
        )
    )
    else -> DeviceConfiguration(
        when (pedal2Type) {
            Slot2PedalType.TAPE_ECHO -> TapeEchoDescriptor
            Slot2PedalType.ANALOG_DELAY -> AnalogDelayDescriptor
            else -> error("unreachable")
        },
        mapOf(
            DeviceParameter.Id.PedalEnabled to pedal2Enabled,
            DeviceParameter.Id.DelayTime to pedal2Dial1.semanticValue.toInt(),
            DeviceParameter.Id.PedalLevel to pedal2Dial2.toInt(),
            DeviceParameter.Id.DelayFeedback to pedal2Dial3.toInt(),
            DeviceParameter.Id.EqTone to pedal2Dial4.toInt(),
            DeviceParameter.Id.ModulationSpeed to pedal2Dial5.toInt(),
            DeviceParameter.Id.ModulationDepth to pedal2Dial6.toInt(),
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
    mapOf(
        DeviceParameter.Id.PedalEnabled to reverbPedalEnabled,
        DeviceParameter.Id.PedalMix to reverbPedalDial1.value.toInt(),
        DeviceParameter.Id.ReverbTime to reverbPedalDial2.value.toInt(),
        DeviceParameter.Id.ReverbPreDelay to reverbPedalDial3.toInt(),
        DeviceParameter.Id.ReverbLowDamp to reverbPedalDial4.value.toInt(),
        DeviceParameter.Id.ReverbHighDamp to reverbPedalDial5.value.toInt(),
    )
)

private fun <T : AmplifierDescriptor> DeviceConfiguration<T>.plus(diff: AmpDialTurnedMessage): DeviceConfiguration<T> {
    return when (diff.dial.toInt()) {
        0x00 -> withValue(DeviceParameter.Id.Gain, diff.value.semanticValue.toInt())
        0x01 -> withValue(DeviceParameter.Id.EqTreble, diff.value.semanticValue.toInt())
        0x02 -> withValue(DeviceParameter.Id.EqMiddle, diff.value.semanticValue.toInt())
        0x03 -> withValue(DeviceParameter.Id.EqBass, diff.value.semanticValue.toInt())
        0x04 -> withValue(DeviceParameter.Id.AmpVolume, diff.value.semanticValue.toInt())
        0x05 -> withValue(
                if (descriptor.presenceIsCalledTone) DeviceParameter.Id.AmpTone else DeviceParameter.Id.AmpPresence,
                diff.value.semanticValue.toInt(),
            )
        0x06 -> withValue(DeviceParameter.Id.Resonance, diff.value.semanticValue.toInt(),)
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
            0x00 -> withValue(DeviceParameter.Id.CompSensitivity, diff.value.asZeroZoTen().value.toInt())
            0x01 -> withValue(DeviceParameter.Id.PedalLevel, diff.value.asZeroZoTen().value.toInt())
            0x02 -> withValue(DeviceParameter.Id.CompAttack, diff.value.asZeroZoTen().value.toInt())
            0x03 -> withValue(DeviceParameter.Id.CompVoice, diff.value.asZeroZoTen().value.toInt().asCompressorVoice())
            else -> error("Unknown compressor dial ${diff.dialIndex}")
        }
        is ChorusPedalDescriptor -> when (diff.dialIndex.toInt()) {
            0x00 -> withValue(DeviceParameter.Id.ModulationSpeed, diff.value.semanticValue.toInt())
            0x01 -> withValue(DeviceParameter.Id.ModulationDepth, diff.value.asZeroZoTen().value.toInt())
            0x02 -> withValue(DeviceParameter.Id.ModulationManual, diff.value.asZeroZoTen().value.toInt())
            0x03 -> withValue(DeviceParameter.Id.PedalMix, diff.value.asZeroZoTen().value.toInt())
            0x04 -> withValue(DeviceParameter.Id.EqLowCut, diff.value.semanticValue.toInt() == 1)
            0x05 -> withValue(DeviceParameter.Id.EqHighCut, diff.value.semanticValue.toInt() == 1)
            else -> error("Unknown chorus pedal dial ${diff.dialIndex}")
        }
        else -> when (diff.dialIndex.toInt()) {
            0x00 -> withValue(DeviceParameter.Id.OverdriveDrive, diff.value.asZeroZoTen().value.toInt())
            0x01 -> withValue(DeviceParameter.Id.EqTone, diff.value.asZeroZoTen().value.toInt())
            0x02 -> withValue(DeviceParameter.Id.PedalLevel, diff.value.asZeroZoTen().value.toInt())
            0x03 -> withValue(DeviceParameter.Id.EqTreble, diff.value.asZeroZoTen().value.toInt())
            0x04 -> withValue(DeviceParameter.Id.EqMiddle, diff.value.asZeroZoTen().value.toInt())
            0x05 -> withValue(DeviceParameter.Id.EqBass, diff.value.asZeroZoTen().value.toInt())
            else -> error("Unknown slot 1 pedal (${descriptor::class.simpleName}) dial ${diff.dialIndex}")
        }
    }
}

private fun <T : AmplifierDescriptor> DeviceConfiguration<T>.plus(diff: NoiseReductionSensitivityChangedMessage): DeviceConfiguration<T> {
    return withValue(DeviceParameter.Id.AmpNoiseReductionSensitivity, diff.sensitivity.value.toInt())
}

private fun <T : SlotTwoPedalDescriptor> DeviceConfiguration<T>.plus(diff: EffectDialTurnedMessage): DeviceConfiguration<T> {
    return when (descriptor) {
        is FlangerPedalDescriptor -> when (diff.dialIndex.toInt()) {
            0x00 -> withValue(DeviceParameter.Id.ModulationSpeed, diff.value.semanticValue.toInt())
            0x01 -> withValue(DeviceParameter.Id.ModulationDepth, diff.value.asZeroZoTen().value.toInt())
            0x02 -> withValue(DeviceParameter.Id.ModulationManual, diff.value.asZeroZoTen().value.toInt())
            0x03 -> withValue(DeviceParameter.Id.EqLowCut, diff.value.semanticValue.toInt() == 1)
            0x04 -> withValue(DeviceParameter.Id.EqHighCut, diff.value.semanticValue.toInt() == 1)
            0x05 -> withValue(DeviceParameter.Id.Resonance, diff.value.asZeroZoTen().value.toInt())
            else -> error("Unknown flanger pedal dial ${diff.dialIndex.hex()}")
        }
        is BlkPhaserDescriptor,
        is OrgPhaserOneDescriptor,
        is OrgPhaserTwoDescriptor -> when (diff.dialIndex.toInt()) {
            0x00 -> withValue(DeviceParameter.Id.ModulationSpeed, diff.value.semanticValue.toInt())
            0x01 -> withValue(DeviceParameter.Id.Resonance, diff.value.asZeroZoTen().value.toInt())
            0x02 -> withValue(DeviceParameter.Id.ModulationManual, diff.value.asZeroZoTen().value.toInt())
            0x03 -> withValue(DeviceParameter.Id.ModulationDepth, diff.value.asZeroZoTen().value.toInt())
            else -> error("Unknown modulation pedal (${descriptor::class.simpleName}) dial ${diff.dialIndex.hex()}")
        }
        is TremoloPedalDescriptor -> when (diff.dialIndex.toInt()) {
            0x00 -> withValue(DeviceParameter.Id.ModulationSpeed, diff.value.semanticValue.toInt())
            0x01 -> withValue(DeviceParameter.Id.Resonance, diff.value.asZeroZoTen().value.toInt())
            0x02 -> withValue(DeviceParameter.Id.ModulationManual, diff.value.asZeroZoTen().value.toInt())
            0x03 -> withValue(DeviceParameter.Id.ModulationDepth, diff.value.asZeroZoTen().value.toInt())
            else -> error("Unknown tremolo pedal dial ${diff.dialIndex.hex()}")
        }
        is DelayPedalDescriptor -> when (diff.dialIndex.toInt()) {
            0x00 -> withValue(DeviceParameter.Id.DelayTime, diff.value.semanticValue.toInt())
            0x01 -> withValue(DeviceParameter.Id.PedalLevel, diff.value.asZeroZoTen().value.toInt())
            0x02 -> withValue(DeviceParameter.Id.DelayFeedback, diff.value.asZeroZoTen().value.toInt())
            0x03 -> withValue(DeviceParameter.Id.EqTone, diff.value.asZeroZoTen().value.toInt())
            0x04 -> withValue(DeviceParameter.Id.ModulationSpeed, diff.value.asZeroZoTen().value.toInt())
            0x05 -> withValue(DeviceParameter.Id.ModulationDepth, diff.value.asZeroZoTen().value.toInt())
            else -> error("Unknown delay pedal (${descriptor::class.simpleName}) dial ${diff.dialIndex.hex()}")
        }
        else -> error("Unknown slot two pedal ${descriptor::class.simpleName}")
    }
}

private fun <T : ReverbPedalDescriptor> DeviceConfiguration<T>.plus(diff: EffectDialTurnedMessage): DeviceConfiguration<T> {
    return when(diff.dialIndex.toInt()) {
        0x00 -> withValue(DeviceParameter.Id.PedalMix, diff.value.asZeroZoTen().value.toInt())
        0x01 -> withValue(DeviceParameter.Id.ReverbTime, diff.value.asZeroZoTen().value.toInt())
        0x02 -> withValue(DeviceParameter.Id.ReverbPreDelay, diff.value.asZeroZoTen().value.toInt())
        0x03 -> withValue(DeviceParameter.Id.ReverbLowDamp, diff.value.asZeroZoTen().value.toInt())
        0x04 -> withValue(DeviceParameter.Id.ReverbHighDamp, diff.value.asZeroZoTen().value.toInt())
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