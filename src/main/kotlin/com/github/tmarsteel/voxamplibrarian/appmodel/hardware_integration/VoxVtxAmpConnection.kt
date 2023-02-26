package com.github.tmarsteel.voxamplibrarian.appmodel.hardware_integration

import com.github.tmarsteel.voxamplibrarian.VOX_AMP_MIDI_DEVICE
import com.github.tmarsteel.voxamplibrarian.appmodel.SimulationConfiguration
import com.github.tmarsteel.voxamplibrarian.appmodel.VtxAmpState
import com.github.tmarsteel.voxamplibrarian.logging.LoggerFactory
import com.github.tmarsteel.voxamplibrarian.protocol.*
import com.github.tmarsteel.voxamplibrarian.protocol.message.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private val logger = LoggerFactory["app-amp-connection"]

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
                    is AmpStateEvent.NewState -> event.state
                    is AmpStateEvent.Message -> try {
                        previousState.plus(event.message)
                    }
                    catch (ex: DifferentialUpdateNotSupportedException) {
                        fetchCurrentState()
                    }
                    is AmpStateEvent.SelectProgramSlot -> {
                        if (previousState is VtxAmpState.ProgramSlotSelected && previousState.slot == event.slot) {
                            return@runningFold previousState
                        }
                        client.exchange(ProgramSlotChangedMessage(event.slot))
                        VtxAmpState.ProgramSlotSelected(
                            previousState.storedUserPrograms,
                            previousState.storedUserPrograms.getValue(event.slot),
                            event.slot
                        )
                    }
                    is AmpStateEvent.PersistConfiguration -> {
                        client.exchange(Exchange.of(
                            WriteUserProgramMessage(event.slot, event.config.toProtocolDataModel()),
                            PersistUserProgramMessage(event.slot)
                        ) { _, _ -> })

                        val stateWithStoredConfig = previousState.withStoredUserProgram(event.slot, event.config)
                        if (previousState is VtxAmpState.ProgramSlotSelected && previousState.slot == event.slot) {
                            stateWithStoredConfig.withActiveConfiguration(event.config)
                        } else {
                            stateWithStoredConfig
                        }
                    }
                }
            }
            .collect { emit(it) }
    }.shareIn(GlobalScope, SharingStarted.Lazily, 1)

    private val setAmpStateRequests = Channel<VtxAmpState>(Channel.BUFFERED)
    private val requestStatePusher: Job = GlobalScope.launch {
        while (true) {
            val nextState = setAmpStateRequests.receive()
            val superseders = setAmpStateRequests.allAvailable()
            val stateToApply = superseders.lastOrNull() ?: nextState
            logger.debug("Applying new amp state (${superseders.size} states were superseded)", stateToApply)

            var baseState = ampState.take(1).single()
            try {
                rebase@while (true) {
                    when (val stateUpdate = baseState.diffTo(stateToApply)) {
                        is AmpStateUpdate.Differential -> {
                            diffs@ for (update in stateUpdate.updates) {
                                logger.info("Differential update: $update")
                                val additionalChanges = update.applyTo(client)
                                baseState = update.applyTo(baseState)
                                if (additionalChanges.isNotEmpty()) {
                                    baseState = additionalChanges.fold(baseState, VtxAmpState::plus)
                                    continue@rebase
                                }
                            }
                            break@rebase
                        }

                        is AmpStateUpdate.FullApply -> {
                            logger.info("Writing full program to update amp state")
                            messages@ for (message in stateUpdate.messagesToApply) {
                                client.exchange(message)
                            }
                            break@rebase
                        }
                    }
                }
            } catch (ex: ExchangeNotAcknowledgedException) {
                logger.error("Failed to apply state. Resetting.", stateToApply, ex)
                ampStateEvents.send(AmpStateEvent.NewState(fetchCurrentState()))
            }
        }
    }

    private suspend fun onMessage(message: MessageToHost) {
        ampStateEvents.send(AmpStateEvent.Message(message))
    }

    private suspend fun fetchCurrentState(): VtxAmpState {
        val currentModeResponse = client.exchange(RequestCurrentModeMessage())
        val activeConfiguration = client.exchange(RequestCurrentProgramMessage()).program.toUiDataModel()
        val userPrograms = ProgramSlot.values().associateWith { client.exchange(RequestUserProgramMessage(it)).program.toUiDataModel() }
        return when(currentModeResponse.mode) {
            CurrentModeResponse.Mode.PROGRAM_SLOT -> {
                VtxAmpState.ProgramSlotSelected(userPrograms, activeConfiguration, currentModeResponse.slot!!)
            }
            CurrentModeResponse.Mode.PRESET -> {
                VtxAmpState.PresetMode(userPrograms, activeConfiguration, currentModeResponse.presetIdentifier!!)
            }
            CurrentModeResponse.Mode.MANUAL -> {
                VtxAmpState.ManualMode(userPrograms, activeConfiguration)
            }
        }
    }

    fun requestState(newState: VtxAmpState) {
        logger.trace("Requesting new amp state", newState)
        setAmpStateRequests.forceSyncSend(newState)
        ampStateEvents.forceSyncSend(AmpStateEvent.NewState(newState))
    }

    fun selectUserProgramSlot(slot: ProgramSlot) {
        ampStateEvents.forceSyncSend(AmpStateEvent.SelectProgramSlot(slot))
    }

    fun persistConfigurationToSlot(configuration: SimulationConfiguration, slot: ProgramSlot) {
        ampStateEvents.forceSyncSend(AmpStateEvent.PersistConfiguration(configuration, slot))
    }

    fun close() {
        requestStatePusher.cancel()
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
    class NewState(val state: VtxAmpState) : AmpStateEvent()
    class SelectProgramSlot(val slot: ProgramSlot) : AmpStateEvent()
    class PersistConfiguration(val config: SimulationConfiguration, val slot: ProgramSlot) : AmpStateEvent()
}

private fun <T> Channel<T>.allAvailable(): List<T> {
    val elements = mutableListOf<T>()
    while (true) {
        val receiveResult = tryReceive()
        if (receiveResult.isSuccess) {
            elements.add(receiveResult.getOrThrow())
        }
        if (receiveResult.isFailure || receiveResult.isClosed) {
            return elements
        }
    }
}

private fun <T> Channel<T>.forceSyncSend(item: T) {
    check(trySend(item).isSuccess)
}