package com.redmadrobot.mapmemory

import com.redmadrobot.mapmemory.internal.getWithNullabilityInference
import com.redmadrobot.mapmemory.internal.putNotNull
import kotlin.reflect.KProperty

public inline fun <reified T> MapMemory.shared(key: String): MapMemoryProperty<T> {
    return object : MapMemoryProperty<T>() {
        override fun getValue(key: String): T = getWithNullabilityInference(key)
        override fun setValue(key: String, value: T) = putNotNull(key, value)
    }.shared(key)
}

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
