package mcchat.packets

sealed class Packet

class InfoPacket(internal val version: Byte) : Packet()

sealed class TopicPacket(internal val topic: String) : Packet()

class SubscriptionPacket(topic: String) : TopicPacket(topic)

class UnsubscriptionPacket(topic: String) : TopicPacket(topic)

class MessagePacket(topic: String, internal val username: String, internal val message: String) : TopicPacket(topic)

@Suppress("CanSealedSubClassBeObject") // TODO Maybe make this an object
class TopicListRequestPacket : Packet()

class TopicListPacket(internal val topics: Array<out String>) : Packet()
