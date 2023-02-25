package com.github.tmarsteel.voxamplibrarian

import org.khronos.webgl.Uint8Array
import org.w3c.files.Blob

class BlobBinaryOutput private constructor(
    private val bufferedOutput: BufferedBinaryOutput,
) : BinaryOutput by bufferedOutput {
    constructor(): this(BufferedBinaryOutput(0xF00, 2))

    fun copyToBlob(): Blob {
        return Blob(arrayOf(
            Uint8Array(bufferedOutput.copyToInput().toArrayOfBytes())
        ))
    }

    companion object {
        fun writeToBlob(writer: (BinaryOutput) -> Unit): Blob {
            val output = BlobBinaryOutput()
            writer(output)
            return output.copyToBlob()
        }
    }
}

private fun BinaryInput.toArrayOfBytes(): Array<Byte> {
    val array = ByteArray(bytesRemaining)
    nextBytes(array)
    return array.toTypedArray()
}