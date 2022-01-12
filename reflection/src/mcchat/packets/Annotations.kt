package mcchat.packets

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
annotation class Position(val position: Int)

@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class OpCode(val opcode: Byte)
