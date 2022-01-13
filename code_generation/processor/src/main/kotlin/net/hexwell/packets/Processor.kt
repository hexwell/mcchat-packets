package net.hexwell.packets

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import java.io.OutputStream

private fun OutputStream.log(str: String) = write("$str\n".toByteArray())

private fun KSTypeReference.qualifiedName(): String {
    return resolve()
        .declaration
        .qualifiedName!!
        .asString()
}

private fun KSAnnotation.qualifiedName(): String = annotationType.qualifiedName()

private inline fun <reified T> Resolver.find(): Map<KSType, KSFunctionDeclaration> {
    return getSymbolsWithAnnotation(T::class.qualifiedName!!)
        .filterIsInstance<KSFunctionDeclaration>()
        .associateBy { it
            .annotations
            .find { it.qualifiedName() == T::class.qualifiedName!! }!!
            .annotationType
            .element!!
            .typeArguments
            .first()
            .type!!
            .resolve()
        }
}

@KotlinPoetKspPreview
class Processor(
    private val codeGenerator: CodeGenerator,
    private val pkg: String,
    private val subpkg: String
) : SymbolProcessor {
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }

        invoked = true

        val log = codeGenerator.createNewFile(
            Dependencies(false),
            "",
            "processor",
            "log"
        )

        log.log("processor started (pkg = $pkg)")

        val packets = resolver
            .getSymbolsWithAnnotation(Packet::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        log.log("found packets: ${ packets.toList() }")

        val fields = resolver
            .getSymbolsWithAnnotation(Field::class.qualifiedName!!)
            .filterIsInstance<KSPropertyDeclaration>()

        log.log("found fields: ${ fields.toList() }")

        val serializers = resolver.find<Serializer<*>>()

        log.log("found serializers: $serializers")

        val deserializers = resolver.find<Deserializer<*>>()

        log.log("found deserializers: $deserializers")

        val functions = packets
            .map {
                log.log("creating serializer for $it")

                val properties = it.getAllProperties().map(KSPropertyDeclaration::simpleName)

                val pfields = fields
                    .filter { properties.contains(it.simpleName) }
                    .sortedBy {
                        @Suppress("UNCHECKED_CAST")
                        it
                            .annotations
                            .find { it.qualifiedName() == Field::class.qualifiedName }!!
                            .arguments
                            .first()
                            .value!! as Comparable<Any>
                    }

                log.log("  fields of packet: ${ pfields.toList() }")

                FunSpec
                    .builder("serialize")
                    .receiver(it
                        .asType(emptyList())
                        .toTypeName()
                    )
                    .apply {
                        pfields.forEach {
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

        log.log("file 'Serialization.kt' created")

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
