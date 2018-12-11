package mcchat.packets

sealed class Packet

class InfoPacket(val version: Byte) : Packet()

sealed class TopicPacket(val topic: String) : Packet()

class SubscriptionPacket(topic: String) : TopicPacket(topic)

class UnsubscriptionPacket(topic: String) : TopicPacket(topic)

class MessagePacket(topic: String, val username: String, val message: String) : TopicPacket(topic)

@Suppress("CanSealedSubClassBeObject") // TODO Maybe make this an object
class TopicListRequestPacket : Packet()

class TopicListPacket(val topics: Array<out String>) : Packet()
