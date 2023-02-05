package com.github.tmarsteel.voxamplibrarian.protocol

import com.github.tmarsteel.voxamplibrarian.BinaryInput
import com.github.tmarsteel.voxamplibrarian.preview

class UnrecognizedMessageException(message: BinaryInput) : RuntimeException("This message is not recognized: ${message.preview()}")