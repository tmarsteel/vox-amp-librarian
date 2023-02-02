package com.github.tmarsteel.voxamplibrarian.protocol

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class AmpDialTurnedMessageTest : StringSpec({
    "parse case 1" {
        val message = AmpDialTurnedMessage.parse(
            "30 00 01 34 41 04 05 40 00".readHexStream()
        )

        message.dial shouldBe 0x05
        message.value shouldBe TwoByteDial(0x40u)
    }

    "parse case 2" {
        val message = AmpDialTurnedMessage.parse(
            "30 00 01 34 41 04 0A 02 00".readHexStream()
        )

        message.dial shouldBe 0x0A
        message.value shouldBe TwoByteDial(0x02u)
    }
})