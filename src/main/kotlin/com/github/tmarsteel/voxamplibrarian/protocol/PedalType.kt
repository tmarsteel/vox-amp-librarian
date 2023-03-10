package com.github.tmarsteel.voxamplibrarian.protocol

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import com.github.tmarsteel.voxamplibrarian.BinaryOutput
import com.github.tmarsteel.voxamplibrarian.hex
import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageParseException
import kotlin.reflect.KMutableProperty1

enum class PedalSlot(
    private val protocolIdentifier: Byte,
    val identifierForEffectDialTurned: Byte,
    val nDials: Int,
    val programEnabledField: KMutableProperty1<in MutableProgram, Boolean>,
) : ProtocolSerializable {
    PEDAL1(0x01, 0x05, 6, MutableProgram::pedal1Enabled),
    PEDAL2(0x02, 0x06, 6, MutableProgram::pedal2Enabled),
    REVERB(0x04, 0x08, 5, MutableProgram::reverbPedalEnabled),
    ;

    override fun writeTo(out: BinaryOutput) {
        out.write(protocolIdentifier)
    }

    companion object {
        fun readFrom(data: BinaryInput): PedalSlot {
            val value = data.nextByte()
            return values().find { it.protocolIdentifier == value }
                ?: throw MessageParseException.InvalidMessage("Unknown pedal slot identifier ${value.hex()}")
        }
    }
}

sealed interface PedalType {
    val slot: PedalSlot
}

enum class Slot1PedalType(val protocolValue: Byte): PedalType {
    COMP(0x00),
    CHORUS(0x01),
    OVERDRIVE(0x02),
    GOLD_DRIVE(0x03),
    TREBLE_BOOST(0x04),
    RC_TURBO(0x05),
    ORANGE_DIST(0x06),
    FAT_DIST(0x07),
    BRIT_LEAD(0x08),
    FUZZ(0x09),
    ;

    override val slot: PedalSlot = PedalSlot.PEDAL1

    companion object {
        fun ofProtocolValue(value: Byte): Slot1PedalType {
            return enumValues<Slot1PedalType>().find { it.protocolValue == value }
                ?: throw NoSuchElementException(
                    "Unknown pedal type for slot 1: $value"
                )
        }
    }
}

enum class Slot2PedalType(val protocolValue: Byte): PedalType {
    FLANGER(0x00),
    BLK_PHASER(0x01),
    ORG_PHASER_1(0x02),
    ORG_PHASER_2(0x03),
    TREMOLO(0x04),
    TAPE_ECHO(0x05),
    ANALOG_DELAY(0x06),
    ;

    override val slot: PedalSlot = PedalSlot.PEDAL2

    companion object {
        fun ofProtocolValue(value: Byte): Slot2PedalType {
            return enumValues<Slot2PedalType>().find { it.protocolValue == value }
                ?: throw NoSuchElementException(
                    "Unknown pedal type for slot 2: $value"
                )
        }
    }
}

enum class ReverbPedalType(val protocolValue: Byte) : PedalType {
    ROOM(0x00),
    SPRING(0x01),
    HALL(0x02),
    PLATE(0x03),
    ;

    override val slot: PedalSlot = PedalSlot.REVERB

    companion object {
        fun ofProtocolValue(value: Byte): ReverbPedalType {
            return enumValues<ReverbPedalType>().find { it.protocolValue == value }
                ?: throw NoSuchElementException(
                    "Unknown pedal type for slot reverb: $value"
                )
        }
    }
}

