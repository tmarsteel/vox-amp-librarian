package com.github.tmarsteel.voxamplibrarian.protocol

value class ZeroToTenDial(val value: Byte) {
    init {
        check(value in 0..100)
    }

    fun asTwoByte(): TwoByteDial = TwoByteDial(value.toUByte().toUShort())
}