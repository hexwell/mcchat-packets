package net.hexwell.packets

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import helpers.findAnnotation
import helpers.getAnnotationArgument
import helpers.getSymbolsWithAnnotationAs
import java.io.InputStream
import java.io.OutputStream

private fun OutputStream.log(str: String) = write("$str\n".toByteArray())

private inline fun <reified A> Resolver.find(): Map<KSType, KSFunctionDeclaration> =
    getSymbolsWithAnnotationAs<A, KSFunctionDeclaration>()
        .associateBy { it
            .findAnnotation<A>()!!
            .annotationType
            .element!!
            .typeArguments
            .first()
            .type!!
            .resolve()
        }

@KotlinPoetKspPreview
class Processor(
    private val codeGenerator: CodeGenerator,
    private val pkg: String,
    private val subpkg: String
) : SymbolProcessor {
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked)
            return emptyList()

        invoked = true

        val log = codeGenerator.createNewFile(
            Dependencies(false),
            "",
            "processor",
            "log"
        )

        log.log("Processor started (pkg = $pkg, subpkg = $subpkg)")

        val packets = resolver
            .getSymbolsWithAnnotationAs<Packet, KSClassDeclaration>()
            .associateBy { it.getAnnotationArgument(Packet::opcode)!! }

        log.log("Found packets: $packets")

        val allFields = resolver.getSymbolsWithAnnotationAs<Field, KSPropertyDeclaration>()

        log.log("Found fields: ${ allFields.toList() }")

        val serializers = resolver.find<Serializer<*>>()

        log.log("Found serializers: $serializers")

        val deserializers = resolver.find<Deserializer<*>>()

        log.log("Found deserializers: $deserializers")

        val serializingFunctions = packets
            .map { (opcode, packet) ->
                log.log("Creating serializer for $packet")

                val properties = packet.getAllProperties().map(KSPropertyDeclaration::simpleName)

                val packetFields = allFields
                    .filter { properties.contains(it.simpleName) }
                    .sortedBy { it.getAnnotationArgument(Field::position)!! }

                log.log("  fields of packet: ${ packetFields.toList() }")

                FunSpec
                    .builder("serialize")
                    .receiver(packet
                        .asType(emptyList())
                        .toTypeName()
                    )
                    .returns(ByteArray::class)
                    .addCode("return byteArrayOf(%L)", opcode)
                    .apply {
                        packetFields.forEach {
                            addCode(
                                " + %N(this.%N)",
                                serializers[it.type.resolve()]!!.simpleName.asString(),
                                it.simpleName.asString()
                            )
                        }
                    }
                    .build()
            }

        val file = FileSpec
            .builder(pkg, "Serialization")
            .apply {
                (serializers.values + deserializers.values).forEach {
                    addImport(subpkg, it.simpleName.asString())
                }
            }
            .apply { serializingFunctions.forEach(::addFunction) }
            .addType(TypeSpec
                .classBuilder("Parser")
                .primaryConstructor(FunSpec
                    .constructorBuilder()
                    .addParameter("input", InputStream::class)
                    .build()
                )
                .addProperty(PropertySpec
                    .builder("input", InputStream::class, KModifier.PRIVATE)
                    .initializer("input")
                    .build()
                )
                .addFunction(FunSpec
                    .builder("next")
                    .returns(resolver
                        .getSymbolsWithAnnotationAs<Base, KSClassDeclaration>()
                        .first()
                        .asType(emptyList())
                        .toTypeName()
                    )
                    .addStatement(
                        "val opcode = %N(input)",
                        deserializers
                            .toList()
                            .find { it
                                .first
                                .declaration
                                .qualifiedName!!
                                .asString() == Byte::class.qualifiedName!!
                            }!!
                            .second
                            .simpleName
                            .asString()
                    )
                    .beginControlFlow("return when (opcode.toInt())")
                    .apply {
                        packets.forEach { (opcode, packet) ->
                            val arguments = packet
                                .getAllProperties()
                                .map {
                                    "${ deserializers[it.type.resolve()]!!.simpleName.asString() }(input)"
                                }
                                .joinToString()

                            addStatement(
                                "%L -> %N($arguments)",
                                opcode,
                                packet.simpleName.asString()
                            )
                        }
                    }
                    .addStatement("else -> throw IllegalArgumentException(\"No packet with opcode \\\"\$opcode\\\" is defined\")")
                    .endControlFlow()
                    .build()
                )
                .build()
            )
            .build()

        file.writeTo(codeGenerator, false)

        log.log("File 'Serialization.kt' created")

        return emptyList()
    }
}

@KotlinPoetKspPreview
class ProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return Processor(
            environment.codeGenerator,
            environment.options["package"]!!,
            environment.options["sub_package"]!!
        )
    }
}
