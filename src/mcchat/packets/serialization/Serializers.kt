package mcchat.packets.serialization

import helpers.nullTerminate
import helpers.terminateWith

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
