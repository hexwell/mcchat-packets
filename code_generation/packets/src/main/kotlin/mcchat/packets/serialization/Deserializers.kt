package mcchat.packets.serialization

import helpers.readByte
import helpers.readUntil
import net.hexwell.packets.Deserializer
import java.io.ByteArrayInputStream
import java.io.InputStream

@Deserializer<Byte>
internal fun deserializeByte(input: InputStream): Byte {
    return input.readByte()
}

@Deserializer<String>
internal fun deserializeString(input: InputStream): String {
    return String(input.readUntil(0))
}

@Deserializer<Array<out String>>
internal fun deserializeStringArray(input: InputStream): Array<out String> {
    val temp = ByteArrayInputStream(input.readUntil(4))

    val data = mutableListOf<String>()

    while (temp.available() != 0) {
        data.add(deserializeString(temp))
    }

    return data.toTypedArray()
}
