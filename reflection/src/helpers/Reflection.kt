package helpers

import kotlin.reflect.KClass

/*
    Workaround for removing the out type projection when accessing the class from an object (<object>::class)

    References:
        https://discuss.kotlinlang.org/t/type-projection-clash-when-accessing-property-delegate-instance/8331
        https://youtrack.jetbrains.com/issue/KT-16432
*/
@Suppress("UNCHECKED_CAST")
val <T : Any> T.kclass
    get() = this::class as KClass<T>

fun <T : Any> KClass<out T>.deepSealedSubclasses(): List<KClass<out T>> {
    return sealedSubclasses.let { it + it.flatMap(KClass<out T>::deepSealedSubclasses) }
}
