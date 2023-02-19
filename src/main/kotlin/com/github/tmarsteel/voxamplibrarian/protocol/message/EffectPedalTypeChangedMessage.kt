package com.github.tmarsteel.voxamplibrarian.protocol.message

import com.github.tmarsteel.voxamplibrarian.*
import com.github.tmarsteel.voxamplibrarian.protocol.*

class EffectPedalTypeChangedMessage(
    val type: PedalType,
) : MessageToHost, MessageToAmp<EffectPedalTypeChangedMessage.Response> {
    override fun createResponseHandler() = object : ResponseHandler<Response> {
        private var ack: GenericAcknowledgement? = null
        private val dialUpdates = mutableListOf<EffectDialTurnedMessage>()
        override fun onMessage(payload: BinaryInput): ResponseHandler.MessageResult<Response> {
            if (ack == null) {
                ack = GenericAcknowledgement.parse(payload)
                return ResponseHandler.MessageResult.MoreMessagesNeeded
            }

            dialUpdates.add(EffectDialTurnedMessage.parse(payload))
            if (dialUpdates.size < 6) {
                return ResponseHandler.MessageResult.MoreMessagesNeeded
            }

            return ResponseHandler.MessageResult.ResponseComplete(Response(
                ack!!,
                dialUpdates,
            ))
        }
    }

    override fun writeTo(out: BinaryOutput) {
        out.write(PREFIX)

        when (type) {
            is Slot1PedalType -> {
                out.write(PedalSlot.PEDAL1)
                out.write(type.protocolValue)
            }
            is Slot2PedalType -> {
                out.write(PedalSlot.PEDAL2)
                out.write(type.protocolValue)
            }
            is ReverbPedalType -> {
                out.write(PedalSlot.REVERB)
                out.write(type.protocolValue)
            }
        }

        out.write(0x00)
    }

    companion object : MidiProtocolMessage.Factory<EffectPedalTypeChangedMessage> {
        val PREFIX = byteArrayOf(0x30, 0x00, 0x01, 0x34, 0x41, 0x03)
        override val type = EffectPedalTypeChangedMessage::class
        override fun parse(fullMessage: BinaryInput): EffectPedalTypeChangedMessage {
            requirePrefix(fullMessage, PREFIX, EffectDialTurnedMessage::class)
            val slot = try {
                PedalSlot.readFrom(fullMessage)
            } catch (ex: MessageParseException.InvalidMessage) {
                throw MessageParseException.PrefixNotRecognized(EffectPedalTypeChangedMessage::class)
            }
            val pedalId = fullMessage.nextByte()
            val pedalType = when (slot) {
                PedalSlot.PEDAL1 -> Slot1PedalType.ofProtocolValue(pedalId)
                PedalSlot.PEDAL2 -> Slot2PedalType.ofProtocolValue(pedalId)
                PedalSlot.REVERB -> ReverbPedalType.ofProtocolValue(pedalId)
            }
            requireNextByteEquals(fullMessage, 0x00)
            requireEOF(fullMessage)

            return EffectPedalTypeChangedMessage(pedalType)
        }
    }

    class Response(
        val acknowledgement: GenericAcknowledgement,
        val dialUpdates: List<EffectDialTurnedMessage>
    )
}