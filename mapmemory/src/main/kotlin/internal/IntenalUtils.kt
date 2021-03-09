package com.redmadrobot.mapmemory.internal

import com.redmadrobot.mapmemory.MapMemory
import kotlin.properties.ReadWriteProperty
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
    crossinline defaultValue: (key: String) -> V,
): ReadWriteProperty<Any?, V> {
    return object : ReadWriteProperty<Any?, V> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): V {
            val key = keyOf(thisRef, property)
            return if (contains(key)) {
                get(key) as V
            } else {
                val value = defaultValue(key)
                put(key, value)
                value
            }
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
            this@getOrPutProperty.setValue(thisRef, property, value)
        }
    }
}

@PublishedApi
internal fun keyOf(thisRef: Any?, property: KProperty<*>): String {
    return if (thisRef != null) {
        "${thisRef.javaClass.canonicalName}#${property.name}"
    } else {
        property.name
    }
}
