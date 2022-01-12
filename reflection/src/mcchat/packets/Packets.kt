@file:Suppress("unused")

package mcchat.packets

sealed class Packet

@OpCode(0)
class InfoPacket(@Position(0) val version: Byte) : Packet()

sealed class TopicPacket(@Position(0) val topic: String) : Packet()

@OpCode(1)
class SubscriptionPacket(topic: String) : TopicPacket(topic)

@OpCode(2)
class UnsubscriptionPacket(topic: String) : TopicPacket(topic)

@OpCode(3)
class MessagePacket(
    topic: String,
    @Position(1) val username: String,
    @Position(2) val message: String,
) : TopicPacket(topic)

@Suppress("CanSealedSubClassBeObject")
@OpCode(4)
class TopicListRequestPacket : Packet()

@OpCode(5)
class TopicListPacket(@Position(0) val topics: Array<out String>) : Packet() {
    companion object {
        const val TERMINATOR: Byte = 4
    }
}
