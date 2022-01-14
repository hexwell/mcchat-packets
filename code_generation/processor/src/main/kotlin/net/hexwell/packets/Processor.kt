package net.hexwell.packets

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import helpers.findAnnotation
import helpers.getSymbolsWithAnnotationAs
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
            .associateBy { it
                .findAnnotation<Packet>()!!
                .arguments
                .first()
                .value as Byte
            }

        log.log("Found packets: $packets")

        val allFields = resolver.getSymbolsWithAnnotationAs<Field, KSPropertyDeclaration>()

        log.log("Found fields: ${ allFields.toList() }")

        val serializers = resolver.find<Serializer<*>>()

        log.log("Found serializers: $serializers")

        val deserializers = resolver.find<Deserializer<*>>()

        log.log("Found deserializers: $deserializers")

        val functions = packets
            .map { (opcode, packet) ->
                log.log("Creating serializer for $packet")

                val properties = packet.getAllProperties().map(KSPropertyDeclaration::simpleName)

                val packetFields = allFields
                    .filter { properties.contains(it.simpleName) }
                    .sortedBy {
                        @Suppress("UNCHECKED_CAST")
                        it
                            .findAnnotation<Field>()!!
                            .arguments
                            .first()
                            .value!! as Comparable<Any>
                    }

                log.log("  fields of packet: ${ packetFields.toList() }")

                FunSpec
                    .builder("serialize")
                    .receiver(packet
                        .asType(emptyList())
                        .toTypeName()
                    )
                    .addCode(CodeBlock.of("println(byteArrayOf($opcode).toList())\n"))
                    .apply {
                        packetFields.forEach {
                            addCode(CodeBlock.of(
                                "println(${serializers[it.type.resolve()]!!.simpleName.asString()}(this.${it.simpleName.asString()}).toList())\n"
                            ))
                        }
                    }
                    .build()
            }

        val file = FileSpec
            .builder(pkg, "Serialization")
            .apply {
                serializers.values.forEach {
                    addImport(subpkg, it.simpleName.asString())
                }
            }
            .apply { functions.forEach(::addFunction) }
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
