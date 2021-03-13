package com.redmadrobot.mapmemory

/**
 * Associates the specified [value] with the specified [name] scoped to the given type [T].
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
 * Returns the value associated with the specified [name] scoped to the given type [T].
 * @see scopedKeyOf
 */
public inline fun <reified T, V> MapMemory.getScoped(name: String): V {
    @Suppress("UNCHECKED_CAST")
    return get(scopedKeyOf<T>(name)) as V
}

/**
 * Creates key with the given [name] scoped to the given type [T].
 * @see mapMemoryOf
 */
public inline fun <reified T> scopedKeyOf(name: String): String = "${T::class.java.name}#$name"
