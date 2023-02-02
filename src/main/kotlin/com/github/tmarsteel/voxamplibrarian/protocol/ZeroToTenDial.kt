package com.github.tmarsteel.voxamplibrarian.protocol

value class ZeroToTenDial(val value: Byte) : ProtocolSerializable {
    init {
        check(value in 0..100)
    }

    fun asTwoByte(): TwoByteDial = TwoByteDial(value.toUByte().toUShort())

    override fun writeTo(out: BinaryOutput) {
        out.write(value)
    }

    companion object {
        fun readFrom(input: BinaryInput): ZeroToTenDial {
            return ZeroToTenDial(input.nextByte())
        }
    }
}