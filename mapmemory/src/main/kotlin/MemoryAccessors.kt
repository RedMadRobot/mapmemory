package com.redmadrobot.mapmemory

import com.redmadrobot.mapmemory.internal.getOrPutProperty
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Creates a delegate for dealing with nullable values stored in [MapMemory].
 * The delegate returns `null` if there is no corresponding value in memory.
 */
public fun <T : Any> MapMemory.nullable(): ReadWriteProperty<Any?, T?> {
    return object : ReadWriteProperty<Any?, T?> {
        @Suppress("UNCHECKED_CAST")
        override fun getValue(thisRef: Any?, property: KProperty<*>): T? = get(property.name) as T?

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
            if (value == null) {
                remove(property.name)
            } else {
                put(property.name, value)
            }
        }
    }
}

/**
 * Creates a delegate for dealing with [MutableMap] stored in [MapMemory].
 * The delegate returns (and stores) empty map if there is no corresponding value in memory.
 */
public fun <K, V> MapMemory.map(): ReadWriteProperty<Any?, MutableMap<K, V>> {
    return getOrPutProperty { ConcurrentHashMap<K, V>() }
}

/**
 * Creates a delegate for dealing with [MutableList] stored in [MapMemory].
 * The delegate returns (and stores) empty list if there is no corresponding value in memory.
 */
public fun <T> MapMemory.list(): ReadWriteProperty<Any?, MutableList<T>> {
    return getOrPutProperty { mutableListOf() }
}
