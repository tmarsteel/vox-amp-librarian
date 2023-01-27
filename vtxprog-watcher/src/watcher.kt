import java.lang.Exception
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.Instant
import kotlin.io.path.*

fun main(vararg args: String) {
    val dir = Paths.get(args[0])
    check(dir.isDirectory())

    val fileLastTransformed = mutableMapOf<Path, Instant>()

    while(true) {
        val filesInDirByName = dir.listDirectoryEntries()
            .filter { it.isRegularFile() }
            .associateBy { it.name }

        filesInDirByName.values
            .filter { it.extension == "vtxprog" }
            .forEach { vtxprogFile ->
                val fileModifiedAt = vtxprogFile.getLastModifiedTime().toInstant()
                val lastTransformed = fileLastTransformed[vtxprogFile] ?: Instant.MIN
                if (fileModifiedAt <= lastTransformed) {
                    return@forEach
                }

                try {
                    transformAndCompare(vtxprogFile)
                    fileLastTransformed[vtxprogFile] = fileModifiedAt
                }
                catch(ex: Exception) {
                    println("Transform failed for ${vtxprogFile.fileName}:")
                    ex.printStackTrace(System.out)
                }
            }

        Thread.sleep(250)
    }
}

val Path.transformedFileName: String get() = "$nameWithoutExtension.vtxprog.first.bin"

val prelude = "56 54 58 50 52 4F 47 31 30 30 30 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
    .split(' ')
    .map { it.toInt(16).toByte() }
    .toByteArray()

fun transformAndCompare(vtxprogFile: Path) {
    check(vtxprogFile.fileSize() == 0x2CAL) { "incorrect file size for $vtxprogFile" }
    val outFile = vtxprogFile.parent.resolve(vtxprogFile.transformedFileName)

    vtxprogFile.inputStream(StandardOpenOption.READ).use { inStream ->
        val preludeFromFile = ByteArray(prelude.size)
        inStream.read(preludeFromFile)
        check(prelude.contentEquals(preludeFromFile))

        val firstProgram = ByteArray(0x3E)
        inStream.read(firstProgram)

        if (outFile.exists()) {
            check(outFile.fileSize() == firstProgram.size.toLong())
            val previousProgram = outFile.readBytes()
            println("Changes in $vtxprogFile:")
            printDiff(previousProgram, firstProgram)
        } else {
            println("transformed ${vtxprogFile.fileName}")
        }

        outFile.outputStream(StandardOpenOption.WRITE).use { outStream ->
            outStream.write(firstProgram)
        }
    }
}

fun printDiff(previous: ByteArray, current: ByteArray) {
    check(previous.size == current.size)
    for (i in previous.indices) {
        if (previous[i] != current[i]) {
            println("Diff at offset 0x${i.toString(16).padStart(4, '0')}")
        }
    }
}