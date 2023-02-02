package com.github.tmarsteel.voxamplibrarian.protocol

data class Program(
    val programName: ProgramName,
    val noiseReductionSensitivity: ZeroToTenDial,
    val ampModel: AmpModel,
    val gain: ZeroToTenDial,
    val treble: ZeroToTenDial,
    val middle: ZeroToTenDial,
    val bass: ZeroToTenDial,
    val volume: ZeroToTenDial,
    val presence: ZeroToTenDial,
    val resonance: ZeroToTenDial,
    val brightCap: Boolean,
    val lowCut: Boolean,
    val midBoost: Boolean,
    val tubeBias: TubeBias,
    val ampClass: AmpClass,
    val pedal1Type: Slot1PedalType,
    val pedal1Dial1: TwoByteDial,
    val pedal1Dial2: Byte,
    val pedal1Dial3: Byte,
    val pedal1Dial4: Byte,
    val pedal1Dial5: Byte,
    val pedal1Dial6: Byte,
    val pedal2Type: Slot2PedalType,
    val pedal2Dial1: TwoByteDial,
    val pedal2Dial2: Byte,
    val pedal2Dial3: Byte,
    val pedal2Dial4: Byte,
    val pedal2Dial5: Byte,
    val pedal2Dial6: Byte,
    val reverbPedalType: ReverbPedalType,
    val reverbPedalDial1: ZeroToTenDial,
    val reverbPedalDial2: ZeroToTenDial,
    val reverbPedalDial3: Byte,
    val reverbPedalDial4: ZeroToTenDial,
    val reverbPedalDial5: ZeroToTenDial,
) : ProtocolSerializable {
    override fun writeTo(out: BinaryOutput) {
        out.write(programName.encoded, 0x00, 0x7)
        out.write(0x00)
        out.write(programName.encoded, 0x07, 0x7)
        out.write(0x00)
        out.write(programName.encoded, 0x0E, 0x2)
        out.write(noiseReductionSensitivity)
        out.write(0x16) // ?? not 100% sure, meaning unclear
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
        out.write(pedal2Dial1)
        out.write(pedal2Dial2)
        out.write(pedal2Dial3)
        out.write(pedal2Dial4)
        out.write(pedal2Dial5)
        out.write(pedal2Dial6)
        out.write(
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00
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
        fun readFrom(input: BinaryInput): Program {
            val encodedProgramName = ByteArray(ProgramName.FIXED_LENGTH)
            input.nextBytes(encodedProgramName, 0x00, 0x07)
            require(input.nextByte() == 0x00.toByte())
            input.nextBytes(encodedProgramName, 0x08, 0x07)
            require(input.nextByte() == 0x00.toByte())
            input.nextBytes(encodedProgramName, 0x10, 0x02)
            val programName = ProgramName.decode(encodedProgramName)

            val nrSens = ZeroToTenDial.readFrom(input)
            input.skip(1)
            val ampModel = AmpModel.readFrom(input)
            val gain = ZeroToTenDial.readFrom(input)
            val treble = ZeroToTenDial.readFrom(input)
            require(input.nextByte() == 0x00.toByte())
            val middle = ZeroToTenDial.readFrom(input)
            val bass = ZeroToTenDial.readFrom(input)
            val volume = ZeroToTenDial.readFrom(input)
            val presence = ZeroToTenDial.readFrom(input)
            val resonance = ZeroToTenDial.readFrom(input)
            val brightCap = input.nextByte().toBoolean()
            val lowCut = input.nextByte().toBoolean()
            require(input.nextByte() == 0x00.toByte())
            val midBoost = input.nextByte().toBoolean()
            val tubeBias = TubeBias.readFrom(input)
            val ampClass = AmpClass.readFrom(input)
            val pedal1Type = Slot1PedalType.ofProtocolValue(input.nextByte())
            val pedal1Dial1 = TwoByteDial.readFrom(input)
            val pedal1Dial2 = input.nextByte()
            input.skip(1)
            val pedal1Dial3 = input.nextByte()
            val pedal1Dial4 = input.nextByte()
            val pedal1Dial5 = input.nextByte()
            val pedal1Dial6 = input.nextByte()
            val pedal2Type = Slot2PedalType.ofProtocolValue(input.nextByte())
            val pedal2Dial1 = TwoByteDial.readFrom(input)
            require(input.nextByte() == 0x00.toByte())
            val pedal2Dial2 = input.nextByte()
            val pedal2Dial3 = input.nextByte()
            val pedal2Dial4 = input.nextByte()
            val pedal2Dial5 = input.nextByte()
            val pedal2Dial6 = input.nextByte()
            input.skip(0x0A)
            val reverbPedalType = ReverbPedalType.ofProtocolValue(input.nextByte())
            val reverbPedalDial1 = ZeroToTenDial.readFrom(input)
            val reverbPedalDial2 = ZeroToTenDial.readFrom(input)
            val reverbPedalDial3 = input.nextByte()
            val reverbPedalDial4 = ZeroToTenDial.readFrom(input)
            val reverbPedalDial5 = ZeroToTenDial.readFrom(input)
            input.skip(1)

            if (input.bytesRemaining != 0) {
                throw MessageParseException.InvalidMessage(
                    "Full program is too large, got ${input.bytesRemaining} extra bytes"
                )
            }

            return Program(
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
                pedal1Type = pedal1Type,
                pedal1Dial1 = pedal1Dial1,
                pedal1Dial2 = pedal1Dial2,
                pedal1Dial3 = pedal1Dial3,
                pedal1Dial4 = pedal1Dial4,
                pedal1Dial5 = pedal1Dial5,
                pedal1Dial6 = pedal1Dial6,
                pedal2Type = pedal2Type,
                pedal2Dial1 = pedal2Dial1,
                pedal2Dial2 = pedal2Dial2,
                pedal2Dial3 = pedal2Dial3,
                pedal2Dial4 = pedal2Dial4,
                pedal2Dial5 = pedal2Dial5,
                pedal2Dial6 = pedal2Dial6,
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