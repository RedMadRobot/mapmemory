package com.redmadrobot.mapmemory.internal

import com.redmadrobot.mapmemory.MapMemory
import com.redmadrobot.mapmemory.MapMemoryProperty

@PublishedApi
internal inline fun <reified V : Any> MapMemory.reusableGetOrPutProperty(
    noinline clear: (V) -> Unit,
    crossinline defaultValue: () -> V,
): MapMemoryProperty<V> = object : MapMemoryProperty<V>() {
    override fun getValue(key: String): V {
        return getOrPut(key) {
            defaultValue().also { value ->
                val clearableValue = ClearableValue(value, clear)
                putReusable(key, clearableValue)
            }
        } as V
    }

    override fun setValue(key: String, value: V) {
        putNotNull(key, value)
        updateReusable(key, value)
    }
}

@PublishedApi
internal data class ClearableValue<V : Any>(
    val value: V,
    val clear: (V) -> Unit,
) {
    fun getCleared(): V = value.apply(clear)
}
