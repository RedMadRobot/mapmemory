package com.redmadrobot.mapmemory.internal

import com.redmadrobot.mapmemory.MapMemory
import com.redmadrobot.mapmemory.MapMemoryProperty
import kotlin.reflect.KProperty

@Suppress("NOTHING_TO_INLINE")
@PublishedApi
internal inline fun MapMemory.putNotNull(key: String, value: Any?) {
    if (value == null) {
        remove(key)
    } else {
        put(key, value)
    }
}

@PublishedApi
internal inline fun <reified V> MapMemory.getWithNullabilityInference(key: String): V {
    return if (null is V) {
        get(key)
    } else {
        getOrElse(key) { throw NoSuchElementException("Key $key is missing in the map.") }
    } as V
}

@PublishedApi
internal inline fun <reified V : Any> MapMemory.getOrPutProperty(
    crossinline defaultValue: () -> V,
): MapMemoryProperty<V> {
    return object : MapMemoryProperty<V>() {
        override fun getValue(key: String): V = getOrPut(key) { defaultValue() } as V
        override fun setValue(key: String, value: V) = putNotNull(key, value)
    }
}

@PublishedApi
internal fun keyOf(thisRef: Any?, property: KProperty<*>): String {
    return if (thisRef != null) {
        "${thisRef.javaClass.name}#${property.name}"
    } else {
        property.name
    }
}
