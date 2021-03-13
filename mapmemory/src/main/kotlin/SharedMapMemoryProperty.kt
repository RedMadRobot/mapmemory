package com.redmadrobot.mapmemory

import com.redmadrobot.mapmemory.internal.getWithNullabilityInference
import com.redmadrobot.mapmemory.internal.putNotNull
import kotlin.reflect.KProperty

/**
 * Returns property delegate to access shared value in memory associated with the given [key].
 * ```
 * var host: String by memory.shared("selectedServer")
 * ```
 */
public inline fun <reified T> MapMemory.shared(key: String): MapMemoryProperty<T> {
    return object : MapMemoryProperty<T>() {
        override fun getValue(key: String): T = getWithNullabilityInference(key)
        override fun setValue(key: String, value: T) = putNotNull(key, value)
    }.shared(key)
}

/**
 * Makes property delegate shared to access value in memory associated with the given [key].
 * ```
 * var answer: Int by memory { 42 }.shared("ultimateAnswer")
 * ```
 */
public fun <V> MapMemoryProperty<V>.shared(key: String): MapMemoryProperty<V> {
    return if (this is SharedMapMemoryProperty) {
        property.shared(key)
    } else {
        SharedMapMemoryProperty(key, this)
    }
}

private class SharedMapMemoryProperty<V>(
    val key: String,
    val property: MapMemoryProperty<V>,
) : MapMemoryProperty<V>() {
    override fun getValue(thisRef: Any?, property: KProperty<*>): V = getValue(key)
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: V) = setValue(key, value)

    override fun getValue(key: String): V = property.getValue(key)
    override fun setValue(key: String, value: V) = property.setValue(key, value)
}
