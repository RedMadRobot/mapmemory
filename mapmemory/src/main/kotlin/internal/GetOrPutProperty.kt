package com.redmadrobot.mapmemory.internal

import com.redmadrobot.mapmemory.MapMemory
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

public inline fun <reified T : Any> MapMemory.getOrPutProperty(
    crossinline defaultValue: () -> T,
): ReadWriteProperty<Any?, T> {
    return object : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return getOrPut(property.name, defaultValue) as T
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            put(property.name, value)
        }
    }
}
