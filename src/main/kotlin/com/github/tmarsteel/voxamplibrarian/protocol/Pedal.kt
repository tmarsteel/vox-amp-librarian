package com.github.tmarsteel.voxamplibrarian.protocol

sealed interface Pedal {
    val slotIdentifier: Byte
}

enum class Slot1Pedal(val protocolValue: Byte) : Pedal {
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

    override val slotIdentifier: Byte = 0x01
}

enum class Slot2Pedal(val protocolValue: Byte): Pedal {
    FLANGER(0x00),
    BLK_PHASE(0x01),
    ORG_PHASER_1(0x02),
    ORG_PHASER_2(0x03),
    TREMOLO(0x04),
    DELAY(0x05),
    ANALOG_DELAY(0x06),
    ;

    override val slotIdentifier: Byte = 0x02
}

enum class ReverbPedal(val protocolValue: Byte) : Pedal {
    ROOM(0x00),
    SPRING(0x01),
    HALL(0x02),
    PLATE(0x04),
    ;

    override val slotIdentifier: Byte = 0x04
}

