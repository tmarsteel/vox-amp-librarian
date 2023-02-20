package com.github.tmarsteel.voxamplibrarian.protocol

import com.github.tmarsteel.voxamplibrarian.protocol.message.ErrorMessage

class ExchangeNotAcknowledgedException(
    val exchange: Exchange<*>,
    val error: ErrorMessage
) : RuntimeException("The amplifier device rejected a message: ${error.message}")