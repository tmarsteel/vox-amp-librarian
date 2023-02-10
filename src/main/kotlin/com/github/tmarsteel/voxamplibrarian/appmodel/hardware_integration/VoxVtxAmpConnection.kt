package com.github.tmarsteel.voxamplibrarian.appmodel.hardware_integration

import com.github.tmarsteel.voxamplibrarian.VOX_AMP_MIDI_DEVICE
import com.github.tmarsteel.voxamplibrarian.appmodel.*
import com.github.tmarsteel.voxamplibrarian.protocol.*
import com.github.tmarsteel.voxamplibrarian.protocol.message.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class VoxVtxAmpConnection(
    private val midiDevice: MidiDevice,
) {
    private val client = VoxVtxAmplifierClient(midiDevice, listener = this::onMessage)

    private val messagesToHost = Channel<MessageToHost>(Channel.BUFFERED)
    val ampState: Flow<VtxAmpState> = flow {
        val state = fetchCurrentState()
        emit(state)
        messagesToHost.consumeAsFlow()
            .runningFold(state) { previousState, message ->
                previousState.plus(message)
            }
            .collect {
                emit(it)
            }
    }.shareIn(GlobalScope, SharingStarted.Lazily, 0)

    private suspend fun onMessage(message: MessageToHost) {
        messagesToHost.send(message)
    }

    private fun VtxAmpState.plus(diff: MessageToHost): VtxAmpState {
        when (diff) {
            is AmpDialTurnedMessage -> {
                return when (diff.dial.toInt()) {
                    0x00 -> {
                        this.withConfiguration(configuration.copy(amplifier = configuration.amplifier.withValue(
                            DeviceParameter.Id.GAIN,
                            diff.value.semanticValue.toInt(),
                        )))
                    }
                    0x01 -> {
                        this.withConfiguration(configuration.copy(amplifier = configuration.amplifier.withValue(
                            DeviceParameter.Id.EQ_TREBLE,
                            diff.value.semanticValue.toInt(),
                        )))
                    }
                    0x02 -> {
                        this.withConfiguration(configuration.copy(amplifier = configuration.amplifier.withValue(
                            DeviceParameter.Id.EQ_MIDDLE,
                            diff.value.semanticValue.toInt(),
                        )))
                    }
                    0x03 -> {
                        this.withConfiguration(configuration.copy(amplifier = configuration.amplifier.withValue(
                            DeviceParameter.Id.EQ_BASS,
                            diff.value.semanticValue.toInt(),
                        )))
                    }
                    0x04 -> {
                        this.withConfiguration(configuration.copy(amplifier = configuration.amplifier.withValue(
                            DeviceParameter.Id.AMP_VOLUME,
                            diff.value.semanticValue.toInt(),
                        )))
                    }
                    0x05 -> {
                        this.withConfiguration(configuration.copy(amplifier = configuration.amplifier.withValue(
                            if (configuration.amplifier.descriptor.presenceIsCalledTone) DeviceParameter.Id.AMP_TONE else DeviceParameter.Id.AMP_PRESENCE,
                            diff.value.semanticValue.toInt(),
                        )))
                    }
                    0x06 -> {
                        this.withConfiguration(configuration.copy(amplifier = configuration.amplifier.withValue(
                            DeviceParameter.Id.RESONANCE,
                            diff.value.semanticValue.toInt(),
                        )))
                    }
                    0x07 -> {
                        this.withConfiguration(configuration.copy(amplifier = configuration.amplifier.withValue(
                            DeviceParameter.Id.AMP_BRIGHT_CAP,
                            diff.value.semanticValue.toInt() == 1
                        )))
                    }
                    0x08 -> {
                        this.withConfiguration(configuration.copy(amplifier = configuration.amplifier.withValue(
                            DeviceParameter.Id.AMP_LOW_CUT,
                            diff.value.semanticValue.toInt() == 1
                        )))
                    }
                    0x09 -> {
                        this.withConfiguration(configuration.copy(amplifier = configuration.amplifier.withValue(
                            DeviceParameter.Id.AMP_MID_BOOST,
                            diff.value.semanticValue.toInt() == 1
                        )))
                    }
                    0x0A -> {
                        this.withConfiguration(configuration.copy(amplifier = configuration.amplifier.withValue(
                            DeviceParameter.Id.AMP_TUBE_BIAS,
                            when (diff.value.semanticValue.toInt()) {
                                0 -> TubeBias.OFF
                                1 -> TubeBias.COLD
                                2 -> TubeBias.HOT
                                else -> error("Unrecognized value for tube bias: ${diff.value}")
                            }
                        )))
                    }
                    0x0B -> {
                        this.withConfiguration(configuration.copy(amplifier = configuration.amplifier.withValue(
                            DeviceParameter.Id.AMP_CLASS,
                            when (diff.value.semanticValue.toInt()) {
                                0 -> AmpClass.A
                                1 -> AmpClass.AB
                                else -> error("Unrecognized value for amp class: ${diff.value}")
                            }
                        )))
                    }
                    else -> {
                        console.error("Unimplemented amp dial ${diff.dial}")
                        this
                    }
                }
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

private fun Program.toConfiguration(): SimulationConfiguration {
    return SimulationConfiguration(
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
        pedalOne = SlotOnePedalDescriptor.DEFAULT, // TODO
        pedalTwo = SlotTwoPedalDescriptor.DEFAULT, // TODO
        reverbPedal = ReverbPedalDescriptor.DEFAULT, // TODO
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