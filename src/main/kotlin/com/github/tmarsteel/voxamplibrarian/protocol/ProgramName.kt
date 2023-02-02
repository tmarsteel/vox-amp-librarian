package com.github.tmarsteel.voxamplibrarian.protocol

value class ProgramName(val name: String) {
    init {
        require(name.length <= FIXED_LENGTH) {
            "Program names must be <= $FIXED_LENGTH chars long"
        }
        require(name.all { it.code in 0..0x7F}) {
            "Program names must consist of ASCII characters only"
        }
    }

    val encoded: ByteArray
        get() = name.padEnd(FIXED_LENGTH, ' ').encodeToByteArray(throwOnInvalidSequence = true)

    companion object {
        val FIXED_LENGTH = 16

        fun decode(fromProtocol: ByteArray): ProgramName {
            require(fromProtocol.size == FIXED_LENGTH)
            return fromProtocol
                .map { it.toChar() }
                .joinToString()
                .let(String::trim)
                .let(::ProgramName)
        }
    }
}