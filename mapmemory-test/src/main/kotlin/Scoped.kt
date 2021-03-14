package com.redmadrobot.mapmemory

import kotlin.reflect.KProperty1

/**
 * Associates the specified [value] with the specified [name] scoped to the given type [T].
 * ```
 * memory.putScoped<Foo>("memoizedValue", 42)
 * ```
 * @see scopedKeyOf
 */
public inline fun <reified T> MapMemory.putScoped(name: String, value: Any?): Any? {
    val key = scopedKeyOf<T>(name)
    return if (value == null) {
        remove(key)
    } else {
        put(key, value)
    }
}

/**
 * Associates the specified [value] with the specified [property] declared in the given type [T].
 * ```
 * memory.putScoped(Foo::memoizedValue, 42)
 * ```
 * @see scopedKeyOf
 */
public inline fun <reified T, V> MapMemory.putScoped(property: KProperty1<T, V>, value: V): V {
    val key = scopedKeyOf(property)
    @Suppress("UNCHECKED_CAST")
    return if (value == null) {
        remove(key)
    } else {
        put(key, value)
    } as V
}

/**
 * Returns the value associated with the specified [name] scoped to the given type [T].
 * ```
 * memory.getScoped<Foo>("memoizedValue")
 * ```
 * @see scopedKeyOf
 */
public inline fun <reified T> MapMemory.getScoped(name: String): Any? {
    return get(scopedKeyOf<T>(name))
}

/**
 * Returns the value associated with the specified [property] declared in the given type [T].
 * ```
 * memory.getScoped(Foo::memoizedValue)
 * ```
 * @see scopedKeyOf
 */
public inline fun <reified T, V> MapMemory.getScoped(property: KProperty1<T, V>): V {
    @Suppress("UNCHECKED_CAST")
    return get(scopedKeyOf(property)) as V
}

/**
 * Creates key for the given [propertyName] scoped to the given type [T].
 * ```
 * val key = scopedKeyOf<Foo>("memoizedValue")
 * assert(key == "Foo#memoizedValue")
 * ```
 * @see mapMemoryOf
 */
public inline fun <reified T> scopedKeyOf(propertyName: String): String = "${T::class.java.name}#$propertyName"

/**
 * Creates key for the given [property] declared in the given type [T].
 * ```
 * val key = scopedKeyOf(Foo::memoizedValue)
 * assert(key == "Foo#memoizedValue")
 * ```
 * @see mapMemoryOf
 */
public inline fun <reified T> scopedKeyOf(property: KProperty1<T, *>): String = "${T::class.java.name}#${property.name}"
