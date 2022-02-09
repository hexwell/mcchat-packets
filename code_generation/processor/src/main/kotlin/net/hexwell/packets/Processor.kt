package net.hexwell.packets

import com.google.auto.service.AutoService
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
class Processor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked)
            return emptyList()

        invoked = true

        logger.info("Processor started")

        val packets = resolver
            .getSymbolsWithAnnotationAs<Packet, KSClassDeclaration>()
            .associateBy { it.getAnnotationArgument(Packet::opcode)!! }

        logger.info("Found packets: $packets")

        val allFields = resolver.getSymbolsWithAnnotationAs<Field, KSPropertyDeclaration>()

        logger.info("Found fields: ${ allFields.toList() }")

        val serializers = resolver.find<Serializer<*>>()

        logger.info("Found serializers: $serializers")

        val deserializers = resolver.find<Deserializer<*>>()

        logger.info("Found deserializers: $deserializers")

        val serializingFunctions = packets
            .map { (opcode, packet) ->
                logger.info("Creating serializer for $packet")

                val properties = packet.getAllProperties().map(KSPropertyDeclaration::simpleName)

                val packetFields = allFields
                    .filter { properties.contains(it.simpleName) }
                    .sortedBy { it.getAnnotationArgument(Field::position)!! }

                logger.info("  fields of packet: ${ packetFields.toList() }")

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
                                " + %N(%N)",
                                serializers[it.type.resolve()]!!.simpleName.asString(),
                                it.simpleName.asString()
                            )
                        }
                    }
                    .build()
            }

        val base = resolver
            .getSymbolsWithAnnotationAs<Base, KSClassDeclaration>()
            .first()

        val file = FileSpec
            .builder(base.packageName.asString(), FILE_NAME)
            .apply {
                (serializers.values + deserializers.values).forEach {
                    addImport(it.packageName.asString(), it.simpleName.asString())
                }
            }
            .apply { serializingFunctions.forEach(::addFunction) }
            .addType(TypeSpec
                .classBuilder("Parser")
                .primaryConstructor(FunSpec
                    .constructorBuilder()
                    .addParameter(PARSER_INPUT_PROPERTY_NAME, PARSER_INPUT_PROPERTY_TYPE)
                    .build()
                )
                .addProperty(PropertySpec
                    .builder(PARSER_INPUT_PROPERTY_NAME, PARSER_INPUT_PROPERTY_TYPE, KModifier.PRIVATE)
                    .initializer(PARSER_INPUT_PROPERTY_NAME)
                    .build()
                )
                .addFunction(FunSpec
                    .builder("next")
                    .returns(base
                        .asType(emptyList())
                        .toTypeName()
                    )
                    .addStatement(
                        "val %N = %N(%N)",
                        OPCODE_VARIABLE_NAME,
                        deserializers
                            .entries
                            .find { it
                                .key
                                .declaration
                                .qualifiedName!!
                                .asString() == Byte::class.qualifiedName!!
                            }!!
                            .value
                            .simpleName
                            .asString(),
                        PARSER_INPUT_PROPERTY_NAME
                    )
                    .beginControlFlow("return when (%N.toInt())", OPCODE_VARIABLE_NAME)
                    .apply {
                        packets.forEach { (opcode, packet) ->
                            val arguments = packet
                                .getAllProperties()
                                .map {
                                    "${ deserializers[it.type.resolve()]!!.simpleName.asString() }($PARSER_INPUT_PROPERTY_NAME)"
                                }
                                .joinToString()

                            addStatement(
                                "%L -> %N($arguments)",
                                opcode,
                                packet.simpleName.asString()
                            )
                        }
                    }
                    .addStatement("else -> throw IllegalArgumentException(\"No packet with opcode \\\"\$$OPCODE_VARIABLE_NAME\\\" is defined\")")
                    .endControlFlow()
                    .build()
                )
                .build()
            )
            .build()

        file.writeTo(codeGenerator, false)

        logger.info("File '$FILE_NAME.kt' created")

        return emptyList()
    }

    companion object {
        private const val FILE_NAME = "Serialization"
        private const val PARSER_INPUT_PROPERTY_NAME = "input"
        private val PARSER_INPUT_PROPERTY_TYPE = InputStream::class
        private const val OPCODE_VARIABLE_NAME = "opcode"
    }
}

@KotlinPoetKspPreview
@AutoService(SymbolProcessorProvider::class)
class ProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return Processor(environment.codeGenerator, environment.logger)
    }
}
