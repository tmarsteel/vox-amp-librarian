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
                            DeviceParameter.Id.PEDAL_ENABLED,
                            diff.enabled,
                        )
                    ))
                    PedalSlot.PEDAL2 -> this.withConfiguration(this.configuration.copy(
                        pedalTwo = this.configuration.pedalTwo.withValue(
                            DeviceParameter.Id.PEDAL_ENABLED,
                            diff.enabled,
                        )
                    ))
                    PedalSlot.REVERB -> this.withConfiguration(this.configuration.copy(
                        reverbPedal = this.configuration.reverbPedal.withValue(
                            DeviceParameter.Id.PEDAL_ENABLED,
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
                DeviceParameter.Id.GAIN to gain.value.toInt(),
                DeviceParameter.Id.EQ_BASS to bass.value.toInt(),
                DeviceParameter.Id.EQ_MIDDLE to middle.value.toInt(),
                DeviceParameter.Id.EQ_TREBLE to treble.value.toInt(),
                DeviceParameter.Id.AMP_VOLUME to volume.value.toInt(),
                DeviceParameter.Id.RESONANCE to resonance.value.toInt(),
                DeviceParameter.Id.AMP_NOISE_REDUCTION_SENSITIVITY to noiseReductionSensitivity.value.toInt(),
                DeviceParameter.Id.AMP_LOW_CUT to lowCut,
                DeviceParameter.Id.AMP_MID_BOOST to midBoost,
                DeviceParameter.Id.AMP_BRIGHT_CAP to brightCap,
                DeviceParameter.Id.AMP_TUBE_BIAS to tubeBias,
                DeviceParameter.Id.AMP_CLASS to ampClass,
                DeviceParameter.Id.AMP_TONE to presence.value.toInt(),
                DeviceParameter.Id.AMP_PRESENCE to presence.value.toInt(),
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
            DeviceParameter.Id.PEDAL_ENABLED to pedal1Enabled,
            DeviceParameter.Id.COMP_SENSITIVITY to pedal1Dial1.asZeroZoTen().value.toInt(),
            DeviceParameter.Id.PEDAL_LEVEL to pedal1Dial2.toInt(),
            DeviceParameter.Id.COMP_ATTACK to pedal1Dial3.toInt(),
            DeviceParameter.Id.COMP_VOICE to pedal1Dial4.toInt().asCompressorVoice(),
        )
    )
    Slot1PedalType.CHORUS -> DeviceConfiguration(
        ChorusPedalDescriptor,
        mapOf(
            DeviceParameter.Id.PEDAL_ENABLED to pedal1Enabled,
            DeviceParameter.Id.MODULATION_SPEED to pedal1Dial1.semanticValue.toInt(),
            DeviceParameter.Id.MODULATION_DEPTH to pedal1Dial2.toInt(),
            DeviceParameter.Id.MODULATION_MANUAL to pedal1Dial3.toInt(),
            DeviceParameter.Id.PEDAL_MIX to pedal1Dial4.toInt(),
            DeviceParameter.Id.EQ_LOW_CUT to (pedal1Dial5.toInt() == 1),
            DeviceParameter.Id.EQ_HIGH_CUT to (pedal1Dial6.toInt() == 1),
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
                DeviceParameter.Id.PEDAL_ENABLED to pedal1Enabled,
                DeviceParameter.Id.OVERDRIVE_DRIVE to pedal1Dial1.asZeroZoTen().value.toInt(),
                DeviceParameter.Id.EQ_TONE to pedal1Dial2.toInt(),
                DeviceParameter.Id.PEDAL_LEVEL to pedal1Dial3.toInt(),
                DeviceParameter.Id.EQ_TREBLE to pedal1Dial4.toInt(),
                DeviceParameter.Id.EQ_MIDDLE to pedal1Dial5.toInt(),
                DeviceParameter.Id.EQ_BASS to pedal1Dial6.toInt(),
            )
        )
    }
}

private val Program.slotTwoPedal: DeviceConfiguration<SlotTwoPedalDescriptor> get() = when(pedal2Type) {
    Slot2PedalType.FLANGER -> DeviceConfiguration(
        FlangerPedalDescriptor,
        mapOf(
            DeviceParameter.Id.PEDAL_ENABLED to pedal2Enabled,
            DeviceParameter.Id.MODULATION_SPEED to pedal2Dial1.semanticValue.toInt(),
            DeviceParameter.Id.MODULATION_DEPTH to pedal2Dial2.toInt(),
            DeviceParameter.Id.MODULATION_MANUAL to pedal2Dial3.toInt(),
            DeviceParameter.Id.EQ_LOW_CUT to (pedal2Dial4.toInt() == 1),
            DeviceParameter.Id.EQ_HIGH_CUT to (pedal2Dial5.toInt() == 1),
            DeviceParameter.Id.RESONANCE to pedal2Dial6.toInt(),
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
            DeviceParameter.Id.PEDAL_ENABLED to pedal2Enabled,
            DeviceParameter.Id.MODULATION_SPEED to pedal2Dial1.semanticValue.toInt(),
            DeviceParameter.Id.RESONANCE to pedal2Dial2.toInt(),
            DeviceParameter.Id.MODULATION_MANUAL to pedal2Dial3.toInt(),
            DeviceParameter.Id.MODULATION_DEPTH to pedal2Dial4.toInt(),
        ),
    )
    Slot2PedalType.TREMOLO -> DeviceConfiguration(
        TremoloPedalDescriptor,
        mapOf(
            DeviceParameter.Id.PEDAL_ENABLED to pedal2Enabled,
            DeviceParameter.Id.MODULATION_SPEED to pedal2Dial1.semanticValue.toInt(),
            DeviceParameter.Id.MODULATION_DEPTH to pedal2Dial2.toInt(),
            DeviceParameter.Id.TREMOLO_DUTY to pedal2Dial3.toInt(),
            DeviceParameter.Id.TREMOLO_SHAPE to pedal2Dial4.toInt(),
            DeviceParameter.Id.PEDAL_LEVEL to pedal2Dial5.toInt(),
        )
    )
    else -> DeviceConfiguration(
        when (pedal2Type) {
            Slot2PedalType.TAPE_ECHO -> TapeEchoDescriptor
            Slot2PedalType.ANALOG_DELAY -> AnalogDelayDescriptor
            else -> error("unreachable")
        },
        mapOf(
            DeviceParameter.Id.PEDAL_ENABLED to pedal2Enabled,
            DeviceParameter.Id.DELAY_TIME to pedal2Dial1.semanticValue.toInt(),
            DeviceParameter.Id.PEDAL_LEVEL to pedal2Dial2.toInt(),
            DeviceParameter.Id.DELAY_FEEDBACK to pedal2Dial3.toInt(),
            DeviceParameter.Id.EQ_TONE to pedal2Dial4.toInt(),
            DeviceParameter.Id.MODULATION_SPEED to pedal2Dial5.toInt(),
            DeviceParameter.Id.MODULATION_DEPTH to pedal2Dial6.toInt(),
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
        DeviceParameter.Id.PEDAL_ENABLED to reverbPedalEnabled,
        DeviceParameter.Id.PEDAL_MIX to reverbPedalDial1.value.toInt(),
        DeviceParameter.Id.REVERB_TIME to reverbPedalDial2.value.toInt(),
        DeviceParameter.Id.REVERB_PRE_DELAY to reverbPedalDial3.toInt(),
        DeviceParameter.Id.REVERB_LOW_DAMP to reverbPedalDial4.value.toInt(),
        DeviceParameter.Id.REVERB_HIGH_DAMP to reverbPedalDial5.value.toInt(),
    )
)

private fun <T : AmplifierDescriptor> DeviceConfiguration<T>.plus(diff: AmpDialTurnedMessage): DeviceConfiguration<T> {
    return when (diff.dial.toInt()) {
        0x00 -> withValue(DeviceParameter.Id.GAIN, diff.value.semanticValue.toInt())
        0x01 -> withValue(DeviceParameter.Id.EQ_TREBLE, diff.value.semanticValue.toInt())
        0x02 -> withValue(DeviceParameter.Id.EQ_MIDDLE, diff.value.semanticValue.toInt())
        0x03 -> withValue(DeviceParameter.Id.EQ_BASS, diff.value.semanticValue.toInt())
        0x04 -> withValue(DeviceParameter.Id.AMP_VOLUME, diff.value.semanticValue.toInt())
        0x05 -> withValue(
                if (descriptor.presenceIsCalledTone) DeviceParameter.Id.AMP_TONE else DeviceParameter.Id.AMP_PRESENCE,
                diff.value.semanticValue.toInt(),
            )
        0x06 -> withValue(DeviceParameter.Id.RESONANCE, diff.value.semanticValue.toInt(),)
        0x07 -> withValue(DeviceParameter.Id.AMP_BRIGHT_CAP, diff.value.semanticValue.toInt() == 1)
        0x08 -> withValue(DeviceParameter.Id.AMP_LOW_CUT, diff.value.semanticValue.toInt() == 1)
        0x09 -> withValue(DeviceParameter.Id.AMP_MID_BOOST, diff.value.semanticValue.toInt() == 1)
        0x0A -> withValue(
                DeviceParameter.Id.AMP_TUBE_BIAS,
                when (diff.value.semanticValue.toInt()) {
                    0 -> TubeBias.OFF
                    1 -> TubeBias.COLD
                    2 -> TubeBias.HOT
                    else -> error("Unrecognized value for tube bias: ${diff.value}")
                }
            )
        0x0B -> withValue(
                DeviceParameter.Id.AMP_CLASS,
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
            0x00 -> withValue(DeviceParameter.Id.COMP_SENSITIVITY, diff.value.asZeroZoTen().value.toInt())
            0x01 -> withValue(DeviceParameter.Id.PEDAL_LEVEL, diff.value.asZeroZoTen().value.toInt())
            0x02 -> withValue(DeviceParameter.Id.COMP_ATTACK, diff.value.asZeroZoTen().value.toInt())
            0x03 -> withValue(DeviceParameter.Id.COMP_VOICE, diff.value.asZeroZoTen().value.toInt().asCompressorVoice())
            else -> error("Unknown compressor dial ${diff.dialIndex}")
        }
        is ChorusPedalDescriptor -> when (diff.dialIndex.toInt()) {
            0x00 -> withValue(DeviceParameter.Id.MODULATION_SPEED, diff.value.semanticValue.toInt())
            0x01 -> withValue(DeviceParameter.Id.MODULATION_DEPTH, diff.value.asZeroZoTen().value.toInt())
            0x02 -> withValue(DeviceParameter.Id.MODULATION_MANUAL, diff.value.asZeroZoTen().value.toInt())
            0x03 -> withValue(DeviceParameter.Id.PEDAL_MIX, diff.value.asZeroZoTen().value.toInt())
            0x04 -> withValue(DeviceParameter.Id.EQ_LOW_CUT, diff.value.semanticValue.toInt() == 1)
            0x05 -> withValue(DeviceParameter.Id.EQ_HIGH_CUT, diff.value.semanticValue.toInt() == 1)
            else -> error("Unknown chorus pedal dial ${diff.dialIndex}")
        }
        else -> when (diff.dialIndex.toInt()) {
            0x00 -> withValue(DeviceParameter.Id.OVERDRIVE_DRIVE, diff.value.asZeroZoTen().value.toInt())
            0x01 -> withValue(DeviceParameter.Id.EQ_TONE, diff.value.asZeroZoTen().value.toInt())
            0x02 -> withValue(DeviceParameter.Id.PEDAL_LEVEL, diff.value.asZeroZoTen().value.toInt())
            0x03 -> withValue(DeviceParameter.Id.EQ_TREBLE, diff.value.asZeroZoTen().value.toInt())
            0x04 -> withValue(DeviceParameter.Id.EQ_MIDDLE, diff.value.asZeroZoTen().value.toInt())
            0x05 -> withValue(DeviceParameter.Id.EQ_BASS, diff.value.asZeroZoTen().value.toInt())
            else -> error("Unknown slot 1 pedal (${descriptor::class.simpleName}) dial ${diff.dialIndex}")
        }
    }
}

private fun <T : AmplifierDescriptor> DeviceConfiguration<T>.plus(diff: NoiseReductionSensitivityChangedMessage): DeviceConfiguration<T> {
    return withValue(DeviceParameter.Id.AMP_NOISE_REDUCTION_SENSITIVITY, diff.sensitivity.value.toInt())
}

private fun <T : SlotTwoPedalDescriptor> DeviceConfiguration<T>.plus(diff: EffectDialTurnedMessage): DeviceConfiguration<T> {
    return when (descriptor) {
        is FlangerPedalDescriptor -> when (diff.dialIndex.toInt()) {
            0x00 -> withValue(DeviceParameter.Id.MODULATION_SPEED, diff.value.semanticValue.toInt())
            0x01 -> withValue(DeviceParameter.Id.MODULATION_DEPTH, diff.value.asZeroZoTen().value.toInt())
            0x02 -> withValue(DeviceParameter.Id.MODULATION_MANUAL, diff.value.asZeroZoTen().value.toInt())
            0x03 -> withValue(DeviceParameter.Id.EQ_LOW_CUT, diff.value.semanticValue.toInt() == 1)
            0x04 -> withValue(DeviceParameter.Id.EQ_HIGH_CUT, diff.value.semanticValue.toInt() == 1)
            0x05 -> withValue(DeviceParameter.Id.RESONANCE, diff.value.asZeroZoTen().value.toInt())
            else -> error("Unknown flanger pedal dial ${diff.dialIndex.hex()}")
        }
        is BlkPhaserDescriptor,
        is OrgPhaserOneDescriptor,
        is OrgPhaserTwoDescriptor -> when (diff.dialIndex.toInt()) {
            0x00 -> withValue(DeviceParameter.Id.MODULATION_SPEED, diff.value.semanticValue.toInt())
            0x01 -> withValue(DeviceParameter.Id.RESONANCE, diff.value.asZeroZoTen().value.toInt())
            0x02 -> withValue(DeviceParameter.Id.MODULATION_MANUAL, diff.value.asZeroZoTen().value.toInt())
            0x03 -> withValue(DeviceParameter.Id.MODULATION_DEPTH, diff.value.asZeroZoTen().value.toInt())
            else -> error("Unknown modulation pedal (${descriptor::class.simpleName}) dial ${diff.dialIndex.hex()}")
        }
        is TremoloPedalDescriptor -> when (diff.dialIndex.toInt()) {
            0x00 -> withValue(DeviceParameter.Id.MODULATION_SPEED, diff.value.semanticValue.toInt())
            0x01 -> withValue(DeviceParameter.Id.RESONANCE, diff.value.asZeroZoTen().value.toInt())
            0x02 -> withValue(DeviceParameter.Id.MODULATION_MANUAL, diff.value.asZeroZoTen().value.toInt())
            0x03 -> withValue(DeviceParameter.Id.MODULATION_DEPTH, diff.value.asZeroZoTen().value.toInt())
            else -> error("Unknown tremolo pedal dial ${diff.dialIndex.hex()}")
        }
        is DelayPedalDescriptor -> when (diff.dialIndex.toInt()) {
            0x00 -> withValue(DeviceParameter.Id.DELAY_TIME, diff.value.semanticValue.toInt())
            0x01 -> withValue(DeviceParameter.Id.PEDAL_LEVEL, diff.value.asZeroZoTen().value.toInt())
            0x02 -> withValue(DeviceParameter.Id.DELAY_FEEDBACK, diff.value.asZeroZoTen().value.toInt())
            0x03 -> withValue(DeviceParameter.Id.EQ_TONE, diff.value.asZeroZoTen().value.toInt())
            0x04 -> withValue(DeviceParameter.Id.MODULATION_SPEED, diff.value.asZeroZoTen().value.toInt())
            0x05 -> withValue(DeviceParameter.Id.MODULATION_DEPTH, diff.value.asZeroZoTen().value.toInt())
            else -> error("Unknown delay pedal (${descriptor::class.simpleName}) dial ${diff.dialIndex.hex()}")
        }
        else -> error("Unknown slot two pedal ${descriptor::class.simpleName}")
    }
}

private fun <T : ReverbPedalDescriptor> DeviceConfiguration<T>.plus(diff: EffectDialTurnedMessage): DeviceConfiguration<T> {
    return when(diff.dialIndex.toInt()) {
        0x00 -> withValue(DeviceParameter.Id.PEDAL_MIX, diff.value.asZeroZoTen().value.toInt())
        0x01 -> withValue(DeviceParameter.Id.REVERB_TIME, diff.value.asZeroZoTen().value.toInt())
        0x02 -> withValue(DeviceParameter.Id.REVERB_PRE_DELAY, diff.value.asZeroZoTen().value.toInt())
        0x03 -> withValue(DeviceParameter.Id.REVERB_LOW_DAMP, diff.value.asZeroZoTen().value.toInt())
        0x04 -> withValue(DeviceParameter.Id.REVERB_HIGH_DAMP, diff.value.asZeroZoTen().value.toInt())
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