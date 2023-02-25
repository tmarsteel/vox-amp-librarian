package com.github.tmarsteel.voxamplibrarian

import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageParseException
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.w3c.files.Blob
import org.w3c.files.FileReader
import kotlin.coroutines.suspendCoroutine

class BlobBinaryInput private constructor(private val uint8Array: Uint8Array) : BinaryInput {
    override var position: Int = 0
        private set

    override fun nextByte(): Byte {
        if (position >= uint8Array.length) {
            throw MessageParseException.InvalidMessage("Message is too small, wanted to read more bytes but none available.")
        }

        return uint8Array[position++].toByte()
    }

    override val bytesRemaining: Int
        get() = uint8Array.length - position

    override fun seekToStart() {
        position = 0
    }

    companion object {
        suspend operator fun invoke(blob: Blob): BlobBinaryInput {
            val reader = FileReader()
            return suspendCoroutine { readerDone ->
                reader.onload = {
                    readerDone.resumeWith(
                        Result.success(
                            BlobBinaryInput(Uint8Array(reader.result as ArrayBuffer))
                        ))
                }
                reader.onerror = {
                    readerDone.resumeWith(Result.failure(reader.error as? Throwable ?: FileReaderErrorException(reader.error)))
                }

                reader.readAsArrayBuffer(blob)
            }
        }
    }

    class FileReaderErrorException(val error: dynamic) : RuntimeException()
}