package net.hexwell.packets

@Target(AnnotationTarget.CLASS)
annotation class Base()

@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class Packet(val opcode: Byte)

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
annotation class Field(val position: Int)

@Target(AnnotationTarget.FUNCTION)
annotation class Serializer<T>

@Target(AnnotationTarget.FUNCTION)
annotation class Deserializer<T>
