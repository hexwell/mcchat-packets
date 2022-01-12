package mcchat.packets.serialization

import helpers.readUntil
import mcchat.packets.TopicListPacket
import java.io.ByteArrayInputStream
import java.io.InputStream

fun InputStream.deserializeString(): String {
    return String(this.readUntil(0))
}

fun InputStream.deserializeStringArray(): Array<out String> {
    val temp = ByteArrayInputStream(readUntil(TopicListPacket.TERMINATOR))

    val out = mutableListOf<String>()

    while (temp.available() != 0)
        out.add(temp.deserializeString())

    return out.toTypedArray()
}
