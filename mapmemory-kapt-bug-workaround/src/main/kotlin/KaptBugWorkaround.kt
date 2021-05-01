package com.redmadrobot.mapmemory

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Returns property delegate to deal with value in [this] MapMemory.
 * Workaround for [KT-46317](https://youtrack.jetbrains.com/issue/KT-46317).
 */
public inline fun <reified V> MapMemory.value(): ReadWriteProperty<Any?, V> {
    return object : ReadWriteProperty<Any?, V> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): V = this@value.getValue(thisRef, property)

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
            this@value.setValue(thisRef, property, value)
        }
    }
}
