package helpers

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeReference
import kotlin.reflect.KProperty1

inline fun <reified A> Resolver.getSymbolsWithAnnotation(): Sequence<KSAnnotated> =
    getSymbolsWithAnnotation(A::class.qualifiedName!!)

inline fun <reified A, reified T> Resolver.getSymbolsWithAnnotationAs(): Sequence<T> =
    getSymbolsWithAnnotation<A>().filterIsInstance<T>()

fun KSTypeReference.qualifiedName(): String =
    resolve()
        .declaration
        .let {
            if (it is KSTypeAlias)
                it.type.resolve().declaration
            else
                it
        }
        .qualifiedName!!
        .asString()

fun KSAnnotation.qualifiedName(): String = annotationType.qualifiedName()

inline fun <reified A> KSAnnotated.findAnnotation(): KSAnnotation? = annotations
    .find { it.qualifiedName() == A::class.qualifiedName!! }

@Suppress("UNCHECKED_CAST")
fun <A, R> KSAnnotation.getArgument(property: KProperty1<A, R>): R? = arguments
    .find { it.name!!.asString() == property.name }?.value as R?

inline fun <reified A, R> KSAnnotated.getAnnotationArgument(property: KProperty1<A, R>): R? =
    findAnnotation<A>()?.getArgument(property)
