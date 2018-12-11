package mcchat.packets.serialization

import mcchat.packets.helpers.nullTerminate
import mcchat.packets.helpers.terminateWith

internal fun Byte.serialize(): ByteArray {
    return byteArrayOf(this)
}

internal fun String.serialize(): ByteArray {
    return this.toByteArray().nullTerminate()
}

internal fun Array<out String>.serialize(): ByteArray {
    return this
        .map { it.toByteArray().nullTerminate() }
        .fold(byteArrayOf(), ByteArray::plus)
        .terminateWith(4)
}
