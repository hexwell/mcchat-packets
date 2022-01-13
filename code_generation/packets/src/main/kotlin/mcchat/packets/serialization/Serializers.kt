package mcchat.packets.serialization

import helpers.nullTerminate
import helpers.terminateWith
import net.hexwell.packets.Serializer

@Serializer<Byte>
internal fun serialize(byte: Byte): ByteArray {
    return byteArrayOf(byte)
}

@Serializer<String>
internal fun serialize(string: String): ByteArray {
    return string.toByteArray().nullTerminate()
}

@Serializer<Array<out String>>
internal fun serialize(stringArray: Array<out String>): ByteArray {
    return stringArray
        .map { it.toByteArray().nullTerminate() }
        .fold(byteArrayOf(), ByteArray::plus)
        .terminateWith(4)
}
