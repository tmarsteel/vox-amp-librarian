package com.github.tmarsteel.voxamplibrarian.protocol

import com.github.tmarsteel.voxamplibrarian.protocol.message.ErrorMessage
import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageToAmp

class MessageNotAcknowledgedException(
    val rejectedRequest: MessageToAmp<*>,
    val error: ErrorMessage
) : RuntimeException("The amplifier device rejected a message: ${error.message}")