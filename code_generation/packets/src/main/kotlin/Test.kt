import mcchat.packets.*

fun main() {
    InfoPacket(0).serialize()

    MessagePacket("", "", "").serialize()

    MessagePacket("", "", "").serialize()
}
