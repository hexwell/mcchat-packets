import mcchat.packets.*
import java.io.ByteArrayInputStream

fun main() {
    println(InfoPacket(0).serialize().toList())

    println(MessagePacket("", "", "").serialize().toList())

    println(MessagePacket("", "", "").serialize().toList())

    println(Parser(ByteArrayInputStream(byteArrayOf(0, 0))).next())
}
