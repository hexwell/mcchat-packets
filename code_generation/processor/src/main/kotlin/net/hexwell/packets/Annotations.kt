package net.hexwell.packets

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
annotation class Field(val position: Int)

@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class Packet(val opcode: Byte)
