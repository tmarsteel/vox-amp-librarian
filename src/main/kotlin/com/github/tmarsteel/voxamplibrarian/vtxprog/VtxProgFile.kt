package com.github.tmarsteel.voxamplibrarian.vtxprog

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import com.github.tmarsteel.voxamplibrarian.BinaryOutput
import com.github.tmarsteel.voxamplibrarian.protocol.*
import com.github.tmarsteel.voxamplibrarian.requireNextByteEquals
import com.github.tmarsteel.voxamplibrarian.toBoolean

data class VtxProgFile(
    val programs: List<Program>
) {
    fun writeToInVtxProgFormat(output: BinaryOutput) {
        output.write(PREFIX)
        programs.forEach { it.writeToInVtxProgFormat(output) }
    }

    private fun Program.writeToInVtxProgFormat(output: BinaryOutput) {
        var flags = 0x00
        if (pedal1Enabled) {
            flags = flags or FLAG_PEDAL_1_ENABLED
        }
        if (pedal2Enabled) {
            flags = flags or FLAG_PEDAL_2_ENABLED
        }
        if (reverbPedalEnabled) {
            flags = flags or FLAG_REVERB_PEDAL_ENABLED
        }

        output.write(programName.encoded)
        output.write(noiseReductionSensitivity)
        output.write(flags.toByte())
        output.write(ampModel)
        output.write(gain)
        output.write(treble)
        output.write(middle)
        output.write(bass)
        output.write(volume)
        output.write(resonance)
        output.write(brightCap)
        output.write(lowCut)
        output.write(midBoost)
        output.write(tubeBias)
        output.write(ampClass)
        output.write(pedal1Type.protocolValue)
        output.write(pedal1Dial1.semanticValue)
        output.write(pedal1Dial2)
        output.write(pedal1Dial3)
        output.write(pedal1Dial4)
        output.write(pedal1Dial5)
        output.write(pedal1Dial6)
        output.write(pedal2Type.protocolValue)
        output.write(pedal2Dial1.semanticValue)
        output.write(pedal2Dial2)
        output.write(pedal2Dial3)
        output.write(pedal2Dial4)
        output.write(pedal2Dial5)
        output.write(pedal2Dial6)
        output.write(byteArrayOf(
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
        ))
        output.write(reverbPedalType.protocolValue)
        output.write(reverbPedalDial1)
        output.write(reverbPedalDial2)
        output.write(reverbPedalDial3)
        output.write(reverbPedalDial4)
        output.write(reverbPedalDial5)
        output.write(0x00)
    }

    private fun BinaryOutput.write(value: UShort) {
        write((value.toInt() and 0xFF).toByte())
        write((value.toInt() shr 8).toByte())
    }

    companion object {
        private val PREFIX = byteArrayOf(
            0x56, 0x54, 0x58, 0x50, 0x52, 0x4F, 0x47, 0x31, 0x30, 0x30, 0x30, 0x20, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        )
        private val FLAG_PEDAL_1_ENABLED = 0b0000_0010
        private val FLAG_PEDAL_2_ENABLED = 0b0000_0100
        private val FLAG_REVERB_PEDAL_ENABLED = 0b0001_0000

        fun readFromInVtxProgFormat(input: BinaryInput): VtxProgFile {
            val prefixFromFile = ByteArray(PREFIX.size)
            input.nextBytes(prefixFromFile)
            if (!prefixFromFile.contentEquals(PREFIX)) {
                throw IllegalArgumentException("Incorrect prefix")
            }

            var programs = mutableListOf<Program>()
            while (input.bytesRemaining > 0) {
                if (input.bytesRemaining < 0x3E) {
                    throw IllegalArgumentException("The input file has an incorrect length, programs are always 0x3E bytes long")
                }

                programs.add(readProgramInVtxProgFormat(input))
            }

            return VtxProgFile(programs)
        }

        private fun readProgramInVtxProgFormat(input: BinaryInput): Program {
            val encodedProgramName = ByteArray(0x10)
            input.nextBytes(encodedProgramName)
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
            requireNextByteEquals(input, 0x00)
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
            requireNextByteEquals(input, 0x00)
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