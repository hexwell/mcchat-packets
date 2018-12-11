package mcchat.packets.helpers

import java.io.IOException
import java.io.InputStream

internal fun ByteArray.terminateWith(terminator: Byte): ByteArray {
    return this + byteArrayOf(terminator)
}

internal fun ByteArray.nullTerminate(): ByteArray {
    return this.terminateWith(0)
}

internal fun InputStream.readByte(): Byte {
    return this.read().also {
        if (it == -1)
            throw IOException("Stream ended while reading")
    }.toByte()
}

internal fun InputStream.readUntil(delimiter: Byte): ByteArray {
    val buffer = mutableListOf<Byte>()

    while (true) {
        val current = this.readByte()

        if (current == delimiter)
            break

        buffer.add(current)
    }

    return buffer.toByteArray()
}
