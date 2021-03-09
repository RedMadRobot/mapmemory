package com.redmadrobot.mapmemory.internal

import com.redmadrobot.mapmemory.MapMemory
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
internal fun keyOf(thisRef: Any?, property: KProperty<*>): String {
    return if (thisRef != null) {
        "${thisRef.javaClass.canonicalName}#${property.name}"
    } else {
        property.name
    }
}
