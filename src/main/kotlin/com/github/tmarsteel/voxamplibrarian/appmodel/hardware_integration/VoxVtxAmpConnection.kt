package com.github.tmarsteel.voxamplibrarian.appmodel.hardware_integration

import com.github.tmarsteel.voxamplibrarian.VOX_AMP_MIDI_DEVICE
import com.github.tmarsteel.voxamplibrarian.appmodel.*
import com.github.tmarsteel.voxamplibrarian.protocol.AmpModel
import com.github.tmarsteel.voxamplibrarian.protocol.MidiDevice
import com.github.tmarsteel.voxamplibrarian.protocol.Program
import com.github.tmarsteel.voxamplibrarian.protocol.VoxVtxAmplifierClient
import com.github.tmarsteel.voxamplibrarian.protocol.message.CurrentModeResponse
import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageToHost
import com.github.tmarsteel.voxamplibrarian.protocol.message.RequestCurrentModeMessage
import com.github.tmarsteel.voxamplibrarian.protocol.message.RequestCurrentProgramMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.runningFold

class VoxVtxAmpConnection(
    private val midiDevice: MidiDevice,
) {
    private val client = VoxVtxAmplifierClient(midiDevice, listener = this::onMessage)

    private val messagesToHost = Channel<MessageToHost>(Channel.BUFFERED)
    val ampState: Flow<VtxAmpState> = flow {
        emit(fetchCurrentState())
    }

    private suspend fun onMessage(message: MessageToHost) {
        messagesToHost.send(message)
    }

    private fun VtxAmpState.plus(diff: MessageToHost): VtxAmpState {
        TODO()
    }

    private suspend fun fetchCurrentState(): VtxAmpState {
        val currentSlotResponse = client.exchange(RequestCurrentModeMessage())
        val currentConfig = client.exchange(RequestCurrentProgramMessage()).program.toConfiguration()
        return when(currentSlotResponse.mode) {
            CurrentModeResponse.Mode.PROGRAM_SLOT -> {
                VtxAmpState.ProgramSlotSelected(currentSlotResponse.slot!!, currentConfig)
            }
            CurrentModeResponse.Mode.PRESET -> {
                VtxAmpState.PresetMode(currentSlotResponse.presetIdentifier!!, currentConfig)
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
                DeviceParameter.Id.AMP_TONE to presence,
                DeviceParameter.Id.AMP_PRESENCE to presence,
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