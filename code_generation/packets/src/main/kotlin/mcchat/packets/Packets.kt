package mcchat.packets

import net.hexwell.packets.Base
import net.hexwell.packets.Field
import net.hexwell.packets.Packet as Pkt


@Base
sealed class Packet

@Pkt(0)
class InfoPacket(@Field(0) val version: Byte) : Packet()

sealed class TopicPacket(@Field(0) val topic: String) : Packet()

@Pkt(1)
class SubscriptionPacket(topic: String) : TopicPacket(topic)

@Pkt(2)
class UnsubscriptionPacket(topic: String) : TopicPacket(topic)

@Pkt(3)
class MessagePacket(
    topic: String,
    @Field(1) val username: String,
    @Field(2) val message: String,
) : TopicPacket(topic)

@Suppress("CanSealedSubClassBeObject")
@Pkt(4)
class TopicListRequestPacket : Packet()

@Pkt(5)
class TopicListPacket(@Field(0) val topics: Array<out String>) : Packet() {
    companion object {
        const val TERMINATOR: Byte = 4
    }
}
