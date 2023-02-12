package com.github.tmarsteel.voxamplibrarian.appmodel.hardware_integration

import com.github.tmarsteel.voxamplibrarian.VOX_AMP_MIDI_DEVICE
import com.github.tmarsteel.voxamplibrarian.appmodel.*
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
        TODO()
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