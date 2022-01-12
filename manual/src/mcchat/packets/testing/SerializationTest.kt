package mcchat.packets.testing

import mcchat.packets.InfoPacket
import mcchat.packets.serialize
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test

internal class SerializationTest {
    @Test
    fun infoPacketSerializationMatchingTest() {
        val serializedPacket: ByteArray = InfoPacket(0.toByte()).serialize()
        val manuallySerializedPacket = ByteArray(2)
        //Serialize Packet manually
        manuallySerializedPacket[0] = 0
        manuallySerializedPacket[1] = 0
        //See if the results match
        assertArrayEquals(manuallySerializedPacket, serializedPacket, "The packets must be equal in structure")
        //Deserialization test section
        val reserializedPacket = InfoPacket(manuallySerializedPacket[1])
        //Check if starter and newly reserialized packet match
        //TODO
    }
}
