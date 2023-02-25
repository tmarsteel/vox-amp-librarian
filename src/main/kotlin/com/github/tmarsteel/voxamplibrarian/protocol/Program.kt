package com.github.tmarsteel.voxamplibrarian.protocol

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import com.github.tmarsteel.voxamplibrarian.BinaryOutput
import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageParseException
import com.github.tmarsteel.voxamplibrarian.requireNextByteEquals
import com.github.tmarsteel.voxamplibrarian.toBoolean

interface Program : ProtocolSerializable {
    val programName: ProgramName
    val noiseReductionSensitivity: ZeroToTenDial
    val ampModel: AmpModel
    val gain: ZeroToTenDial
    val treble: ZeroToTenDial
    val middle: ZeroToTenDial
    val bass: ZeroToTenDial
    val volume: ZeroToTenDial
    val presence: ZeroToTenDial
    val resonance: ZeroToTenDial
    val brightCap: Boolean
    val lowCut: Boolean
    val midBoost: Boolean
    val tubeBias: TubeBias
    val ampClass: AmpClass
    val pedal1Enabled: Boolean
    val pedal1Type: Slot1PedalType
    val pedal1Dial1: TwoByteDial
    val pedal1Dial2: Byte
    val pedal1Dial3: Byte
    val pedal1Dial4: Byte
    val pedal1Dial5: Byte
    val pedal1Dial6: Byte
    val pedal2Enabled: Boolean
    val pedal2Type: Slot2PedalType
    val pedal2Dial1: TwoByteDial
    val pedal2Dial2: Byte
    val pedal2Dial3: Byte
    val pedal2Dial4: Byte
    val pedal2Dial5: Byte
    val pedal2Dial6: Byte
    val reverbPedalEnabled: Boolean
    val reverbPedalType: ReverbPedalType
    val reverbPedalDial1: ZeroToTenDial
    val reverbPedalDial2: ZeroToTenDial
    val reverbPedalDial3: Byte
    val reverbPedalDial4: ZeroToTenDial
    val reverbPedalDial5: ZeroToTenDial

    override fun writeTo(out: BinaryOutput) {
        var flags = 0x00
        if (pedal1Enabled) {
            flags = flags or Program.FLAG_PEDAL_1_ENABLED
        }
        if (pedal2Enabled) {
            flags = flags or Program.FLAG_PEDAL_2_ENABLED
        }
        if (reverbPedalEnabled) {
            flags = flags or Program.FLAG_REVERB_PEDAL_ENABLED
        }

        out.write(programName.encoded, 0x00, 0x7)
        out.write(0x00)
        out.write(programName.encoded, 0x07, 0x7)
        out.write(0x00)
        out.write(programName.encoded, 0x0E, 0x2)
        out.write(noiseReductionSensitivity)
        out.write(flags.toByte())
        out.write(ampModel)
        out.write(gain)
        out.write(treble)
        out.write(0x00) // 8th byte
        out.write(middle)
        out.write(bass)
        out.write(volume)
        out.write(presence)
        out.write(resonance)
        out.write(brightCap)
        out.write(lowCut)
        out.write(0x00) // 8th byte
        out.write(midBoost)
        out.write(tubeBias)
        out.write(ampClass)
        out.write(pedal1Type.protocolValue)
        out.write(pedal1Dial1)
        out.write(pedal1Dial2)
        out.write(0x00)
        out.write(pedal1Dial3)
        out.write(pedal1Dial4)
        out.write(pedal1Dial5)
        out.write(pedal1Dial6)
        out.write(pedal2Type.protocolValue)
        out.write(pedal2Dial1.semanticValue)
        out.write(0x00)
        out.write(pedal2Dial2)
        out.write(pedal2Dial3)
        out.write(pedal2Dial4)
        out.write(pedal2Dial5)
        out.write(pedal2Dial6)
        out.write(
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00
        )
        out.write(reverbPedalType.protocolValue)
        out.write(0x00)
        out.write(reverbPedalDial1)
        out.write(reverbPedalDial2)
        out.write(reverbPedalDial3)
        out.write(reverbPedalDial4)
        out.write(reverbPedalDial5)
        out.write(0x00)
    }

    companion object {
        private val FLAG_PEDAL_1_ENABLED = 0b0000_0010
        private val FLAG_PEDAL_2_ENABLED = 0b0000_0100
        private val FLAG_REVERB_PEDAL_ENABLED = 0b0001_0000

        fun readFrom(input: BinaryInput): Program {
            val encodedProgramName = ByteArray(ProgramName.FIXED_LENGTH)
            input.nextBytes(encodedProgramName, 0x00, 0x07)
            require(input.nextByte() == 0x00.toByte())
            input.nextBytes(encodedProgramName, 0x07, 0x07)
            require(input.nextByte() == 0x00.toByte())
            input.nextBytes(encodedProgramName, 0x0E, 0x02)
            val programName = ProgramName.decode(encodedProgramName)

            val nrSens = ZeroToTenDial.readFrom(input)
            val flags = input.nextByte().toInt()
            val ampModel = AmpModel.readFrom(input)
            val gain = ZeroToTenDial.readFrom(input)
            val treble = ZeroToTenDial.readFrom(input)
            requireNextByteEquals(input, 0x00)
            val middle = ZeroToTenDial.readFrom(input)
            val bass = ZeroToTenDial.readFrom(input)
            val volume = ZeroToTenDial.readFrom(input)
            val presence = ZeroToTenDial.readFrom(input)
            val resonance = ZeroToTenDial.readFrom(input)
            val brightCap = input.nextByte().toBoolean()
            val lowCut = input.nextByte().toBoolean()
            requireNextByteEquals(input, 0x00, 0x10)
            val midBoost = input.nextByte().toBoolean()
            val tubeBias = TubeBias.readFrom(input)
            val ampClass = AmpClass.readFrom(input)
            val pedal1Type = Slot1PedalType.ofProtocolValue(input.nextByte())
            val pedal1Dial1 = TwoByteDial.readFrom(input)
            val pedal1Dial2 = input.nextByte()
            val pedal2Dial1Offset: UShort = when(val indicator = input.nextByte().toInt()) {
                0x00 -> 0u
                0x20 -> 128u
                else -> throw MessageParseException.InvalidMessage("Unrecognized offset indicator for pedal 2 dial 1: expected 0x00 or 0x20, got $indicator")
            }
            val pedal1Dial3 = input.nextByte()
            val pedal1Dial4 = input.nextByte()
            val pedal1Dial5 = input.nextByte()
            val pedal1Dial6 = input.nextByte()
            val pedal2Type = Slot2PedalType.ofProtocolValue(input.nextByte())
            val pedal2Dial1 = TwoByteDial((input.nextUShort() + pedal2Dial1Offset).toUShort())
            requireNextByteEquals(input, 0x00)
            val pedal2Dial2 = input.nextByte()
            val pedal2Dial3 = input.nextByte()
            val pedal2Dial4 = input.nextByte()
            val pedal2Dial5 = input.nextByte()
            val pedal2Dial6 = input.nextByte()
            input.skip(0x09)
            val reverbPedalType = ReverbPedalType.ofProtocolValue(input.nextByte())
            input.skip(1)
            val reverbPedalDial1 = ZeroToTenDial.readFrom(input)
            val reverbPedalDial2 = ZeroToTenDial.readFrom(input)
            val reverbPedalDial3 = input.nextByte()
            val reverbPedalDial4 = ZeroToTenDial.readFrom(input)
            val reverbPedalDial5 = ZeroToTenDial.readFrom(input)
            input.skip(1)

            return ProgramImpl(
                programName = programName,
                noiseReductionSensitivity = nrSens,
                ampModel = ampModel,
                gain = gain,
                treble = treble,
                middle = middle,
                bass = bass,
                volume = volume,
                presence = presence,
                resonance = resonance,
                brightCap = brightCap,
                lowCut = lowCut,
                midBoost = midBoost,
                tubeBias = tubeBias,
                ampClass = ampClass,
                pedal1Enabled = flags and FLAG_PEDAL_1_ENABLED > 0,
                pedal1Type = pedal1Type,
                pedal1Dial1 = pedal1Dial1,
                pedal1Dial2 = pedal1Dial2,
                pedal1Dial3 = pedal1Dial3,
                pedal1Dial4 = pedal1Dial4,
                pedal1Dial5 = pedal1Dial5,
                pedal1Dial6 = pedal1Dial6,
                pedal2Enabled = flags and FLAG_PEDAL_2_ENABLED > 0,
                pedal2Type = pedal2Type,
                pedal2Dial1 = pedal2Dial1,
                pedal2Dial2 = pedal2Dial2,
                pedal2Dial3 = pedal2Dial3,
                pedal2Dial4 = pedal2Dial4,
                pedal2Dial5 = pedal2Dial5,
                pedal2Dial6 = pedal2Dial6,
                reverbPedalEnabled = flags and FLAG_REVERB_PEDAL_ENABLED > 0,
                reverbPedalType = reverbPedalType,
                reverbPedalDial1 = reverbPedalDial1,
                reverbPedalDial2 = reverbPedalDial2,
                reverbPedalDial3 = reverbPedalDial3,
                reverbPedalDial4 = reverbPedalDial4,
                reverbPedalDial5 = reverbPedalDial5,
            )
        }
    }
}

interface MutableProgram : Program {
    override var programName: ProgramName
    override var noiseReductionSensitivity: ZeroToTenDial
    override var ampModel: AmpModel
    override var gain: ZeroToTenDial
    override var treble: ZeroToTenDial
    override var middle: ZeroToTenDial
    override var bass: ZeroToTenDial
    override var volume: ZeroToTenDial
    override var presence: ZeroToTenDial
    override var resonance: ZeroToTenDial
    override var brightCap: Boolean
    override var lowCut: Boolean
    override var midBoost: Boolean
    override var tubeBias: TubeBias
    override var ampClass: AmpClass
    override var pedal1Enabled: Boolean
    override var pedal1Type: Slot1PedalType
    override var pedal1Dial1: TwoByteDial
    override var pedal1Dial2: Byte
    override var pedal1Dial3: Byte
    override var pedal1Dial4: Byte
    override var pedal1Dial5: Byte
    override var pedal1Dial6: Byte
    override var pedal2Enabled: Boolean
    override var pedal2Type: Slot2PedalType
    override var pedal2Dial1: TwoByteDial
    override var pedal2Dial2: Byte
    override var pedal2Dial3: Byte
    override var pedal2Dial4: Byte
    override var pedal2Dial5: Byte
    override var pedal2Dial6: Byte
    override var reverbPedalEnabled: Boolean
    override var reverbPedalType: ReverbPedalType
    override var reverbPedalDial1: ZeroToTenDial
    override var reverbPedalDial2: ZeroToTenDial
    override var reverbPedalDial3: Byte
    override var reverbPedalDial4: ZeroToTenDial
    override var reverbPedalDial5: ZeroToTenDial
}

class ProgramImpl(
    override var programName: ProgramName,
    override var noiseReductionSensitivity: ZeroToTenDial,
    override var ampModel: AmpModel,
    override var gain: ZeroToTenDial,
    override var treble: ZeroToTenDial,
    override var middle: ZeroToTenDial,
    override var bass: ZeroToTenDial,
    override var volume: ZeroToTenDial,
    override var presence: ZeroToTenDial,
    override var resonance: ZeroToTenDial,
    override var brightCap: Boolean,
    override var lowCut: Boolean,
    override var midBoost: Boolean,
    override var tubeBias: TubeBias,
    override var ampClass: AmpClass,
    override var pedal1Enabled: Boolean,
    override var pedal1Type: Slot1PedalType,
    override var pedal1Dial1: TwoByteDial,
    override var pedal1Dial2: Byte,
    override var pedal1Dial3: Byte,
    override var pedal1Dial4: Byte,
    override var pedal1Dial5: Byte,
    override var pedal1Dial6: Byte,
    override var pedal2Enabled: Boolean,
    override var pedal2Type: Slot2PedalType,
    override var pedal2Dial1: TwoByteDial,
    override var pedal2Dial2: Byte,
    override var pedal2Dial3: Byte,
    override var pedal2Dial4: Byte,
    override var pedal2Dial5: Byte,
    override var pedal2Dial6: Byte,
    override var reverbPedalEnabled: Boolean,
    override var reverbPedalType: ReverbPedalType,
    override var reverbPedalDial1: ZeroToTenDial,
    override var reverbPedalDial2: ZeroToTenDial,
    override var reverbPedalDial3: Byte,
    override var reverbPedalDial4: ZeroToTenDial,
    override var reverbPedalDial5: ZeroToTenDial,
) : MutableProgram