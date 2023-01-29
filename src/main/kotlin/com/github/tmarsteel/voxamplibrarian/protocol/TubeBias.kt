package com.github.tmarsteel.voxamplibrarian.protocol

enum class TubeBias(val protocolValue: Byte) {
    OFF(0x00),
    COLD(0x01),
    HOT(0x02),
}