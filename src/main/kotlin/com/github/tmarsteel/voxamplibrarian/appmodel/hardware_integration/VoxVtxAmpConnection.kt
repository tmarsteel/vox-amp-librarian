package com.github.tmarsteel.voxamplibrarian.appmodel.hardware_integration

import com.github.tmarsteel.voxamplibrarian.VOX_AMP_MIDI_DEVICE
import com.github.tmarsteel.voxamplibrarian.appmodel.VtxAmpState
import com.github.tmarsteel.voxamplibrarian.protocol.MidiDevice
import com.github.tmarsteel.voxamplibrarian.protocol.VoxVtxAmplifierClient
import com.github.tmarsteel.voxamplibrarian.protocol.message.CurrentModeResponse
import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageToHost
import com.github.tmarsteel.voxamplibrarian.protocol.message.RequestCurrentModeMessage
import com.github.tmarsteel.voxamplibrarian.protocol.message.RequestCurrentProgramMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class VoxVtxAmpConnection(
    midiDevice: MidiDevice,
) {
    private val client = VoxVtxAmplifierClient(midiDevice, listener = this::onMessage)

    private val ampStateEvents = Channel<AmpStateEvent>(Channel.BUFFERED)
    val ampState: SharedFlow<VtxAmpState> = flow {
        val state = fetchCurrentState()
        emit(state)
        ampStateEvents.consumeAsFlow()
            .runningFold(state) { previousState, event ->
                when (event) {
                    is AmpStateEvent.NewStateAcked -> event.state
                    is AmpStateEvent.Message -> try {
                        previousState.plus(event.message)
                    }
                    catch (ex: DifferentialUpdateNotSupportedException) {
                        fetchCurrentState()
                    }
                }
            }
            .collect { emit(it) }
    }.shareIn(GlobalScope, SharingStarted.Lazily, 1)

    private suspend fun onMessage(message: MessageToHost) {
        ampStateEvents.send(AmpStateEvent.Message(message))
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

    suspend fun setState(value: VtxAmpState) {
        val currentState = ampState.take(1).single()
        for (updateMessage in currentState.diffTo(value)) {
            client.exchange(updateMessage)
        }
        ampStateEvents.send(AmpStateEvent.NewStateAcked(value))
    }

    fun close() {
        TODO()
    }

    companion object {
        val VOX_AMP: StateFlow<VoxVtxAmpConnection?> = VOX_AMP_MIDI_DEVICE
            .runningFold<MidiDevice?, VoxVtxAmpConnection?>(null) { currentConnection, midiDevice ->
                currentConnection?.close()

                if (midiDevice == null) {
                    return@runningFold null
                }

                VoxVtxAmpConnection(midiDevice)
            }
            .stateIn(GlobalScope, SharingStarted.Lazily, null)
    }
}

private sealed class AmpStateEvent {
    class Message(val message: MessageToHost) : AmpStateEvent()
    class NewStateAcked(val state: VtxAmpState) : AmpStateEvent()
}