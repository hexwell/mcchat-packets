package mcchat.packets.serialization

import helpers.readByte
import helpers.readUntil
import java.io.ByteArrayInputStream
import java.io.InputStream

internal fun InputStream.deserializeByte(): Byte {
    return this.readByte()
}

internal fun InputStream.deserializeString(): String {
    return String(this.readUntil(0))
}

internal fun InputStream.deserializeStringArray(): Array<out String> {
    val temp = ByteArrayInputStream(readUntil(4))

    val data = mutableListOf<String>()

    while (temp.available() != 0) {
        data.add(temp.deserializeString())
    }

    return data.toTypedArray()
}
