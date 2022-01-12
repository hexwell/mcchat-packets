package mcchat.packets.serialization

import helpers.flatten
import helpers.nullTerminate
import helpers.terminateWith
import mcchat.packets.TopicListPacket
import kotlin.reflect.KProperty1

internal fun <T> KProperty1<in T, *>.serializeAsByteFrom(obj: T): ByteArray {
    return byteArrayOf(this.get(obj) as Byte)
}

fun serializeString(string: String): ByteArray {
    return string.toByteArray().nullTerminate()
}

internal fun <T> KProperty1<in T, *>.serializeAsStringFrom(obj: T): ByteArray {
    return serializeString(this.get(obj) as String)
}

internal fun <T> KProperty1<in T, *>.serializeAsStringArrayFrom(obj: T): ByteArray {
    @Suppress("UNCHECKED_CAST")
    return (this.get(obj) as Array<out String>)
        .map(::serializeString)
        .flatten()
        .terminateWith(TopicListPacket.TERMINATOR)
}
