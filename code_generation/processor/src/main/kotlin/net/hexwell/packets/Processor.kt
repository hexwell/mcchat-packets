package net.hexwell.packets

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import java.io.OutputStream

private fun OutputStream.log(str: String) = write("$str\n".toByteArray())

@KotlinPoetKspPreview
class Processor(private val codeGenerator: CodeGenerator, private val pkg: String) : SymbolProcessor {
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

        log.log("Found packets: ${ packets.toList() }")

        val fields = resolver
            .getSymbolsWithAnnotation(Field::class.qualifiedName!!)
            .filterIsInstance<KSPropertyDeclaration>()

        log.log("Found fields: ${ fields.toList() }")

        val functions = packets
            .map {
                log.log("creating serializer for $it")

                val properties = it.getAllProperties()

                val pfields = fields
                    .filter { properties.contains(it) }
                    .sortedBy {
                        @Suppress("UNCHECKED_CAST")
                        it
                            .annotations
                            .find { it.shortName.asString() == Field::class.simpleName }!!
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
                    .addCode(CodeBlock.of("println(this)"))
                    .build()
            }

        val file = FileSpec
            .builder(pkg, "Serialization")
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
        return Processor(environment.codeGenerator, environment.options["package"]!!)
    }
}
