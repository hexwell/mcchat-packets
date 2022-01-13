package mcchat.packets

import net.hexwell.packets.Field

sealed class Packet

@net.hexwell.packets.Packet(0)
class InfoPacket(@Field(0) val version: Byte) : Packet()

sealed class TopicPacket(@Field(0) val topic: String) : Packet()

@net.hexwell.packets.Packet(1)
class SubscriptionPacket(topic: String) : TopicPacket(topic)

@net.hexwell.packets.Packet(2)
class UnsubscriptionPacket(topic: String) : TopicPacket(topic)

@net.hexwell.packets.Packet(3)
class MessagePacket(
    topic: String,
    @Field(1) val username: String,
    @Field(2) val message: String,
) : TopicPacket(topic)

@Suppress("CanSealedSubClassBeObject")
@net.hexwell.packets.Packet(4)
class TopicListRequestPacket : Packet()

@net.hexwell.packets.Packet(5)
class TopicListPacket(@Field(0) val topics: Array<out String>) : Packet() {
    companion object {
        const val TERMINATOR: Byte = 4
    }
}
