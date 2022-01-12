package mcchat.packets

import mcchat.packets.serialization.deserializeByte
import mcchat.packets.serialization.deserializeString
import mcchat.packets.serialization.deserializeStringArray
import mcchat.packets.serialization.serialize
import java.io.InputStream

class Parser(private val input: InputStream) {
    fun next(): Packet {
        val opcode = input.deserializeByte()

        return when (opcode.toInt()) {
            0 -> InfoPacket(input.deserializeByte())

            1 -> SubscriptionPacket(input.deserializeString())

            2 -> UnsubscriptionPacket(input.deserializeString())

            3 -> MessagePacket(input.deserializeString(), input.deserializeString(), input.deserializeString())

            4 -> TopicListRequestPacket()

            5 -> TopicListPacket(input.deserializeStringArray())

            else -> throw IllegalArgumentException("No packet with opcode \"$opcode\" is defined")
        }
    }
}

fun InfoPacket.serialize(): ByteArray {
    return byteArrayOf(0) + this.version.serialize()
}

fun SubscriptionPacket.serialize(): ByteArray {
    return byteArrayOf(1) + this.topic.serialize()
}

fun UnsubscriptionPacket.serialize(): ByteArray {
    return byteArrayOf(2) + this.topic.serialize()
}

fun MessagePacket.serialize(): ByteArray {
    return byteArrayOf(3) + this.topic.serialize() + this.username.serialize() + this.message.serialize()
}

fun TopicListRequestPacket.serialize(): ByteArray {
    return byteArrayOf(4)
}

fun TopicListPacket.serialize(): ByteArray {
    return byteArrayOf(5) + this.topics.serialize()
}
