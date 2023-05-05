package com.redmadrobot.mapmemory

import com.redmadrobot.mapmemory.internal.getWithNullabilityInference
import com.redmadrobot.mapmemory.internal.putNotNull
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Creates a delegate for dealing with [Map] stored in [MapMemory].
 * The delegate returns (and stores) empty map if there is no corresponding value in `MapMemory`.
 */
public fun <K, V> MapMemory.map(): MapMemoryProperty<Map<K, V>> {
    return invoke { emptyMap() }
}

/**
 * Creates a delegate for dealing with [MutableMap] stored in [MapMemory].
 * The delegate returns (and stores) map with [defaultValue] inside
 * if there is no corresponding value in `MapMemory`.
 * Uses [ConcurrentHashMap] implementation.
 *
 * The property is _reusable_.
 */
public fun <K, V> MapMemory.mutableMap(
    defaultValue: () -> Map<K, V> = ::emptyMap,
): MapMemoryProperty<MutableMap<K, V>> {
    return invoke(
        clear = { map ->
            synchronized(map) {
                map.clear()
                map.putAll(defaultValue())
            }
        },
        defaultValue = { ConcurrentHashMap<K, V>(defaultValue()) },
    )
}

/**
 * Creates a delegate for dealing with [List] stored in [MapMemory].
 * The delegate returns (and stores) empty list if there is no corresponding value in `MapMemory`.
 */
public fun <T> MapMemory.list(): MapMemoryProperty<List<T>> {
    return invoke { emptyList() }
}

/**
 * Creates a delegate for dealing with [MutableList] stored in [MapMemory].
 * The delegate returns (and stores) list with [defaultValue] inside
 * if there is no corresponding value in `MapMemory`.
 * Uses synchronized `ArrayList` implementation.
 *
 * The property is _reusable_.
 */
public fun <T> MapMemory.mutableList(defaultValue: () -> List<T> = ::emptyList): MapMemoryProperty<MutableList<T>> {
    return invoke(
        clear = { list ->
            synchronized(list) {
                list.clear()
                list.addAll(defaultValue())
            }
        },
        defaultValue = { Collections.synchronizedList(defaultValue().toMutableList()) },
    )
}

/**
 * Creates a delegate for dealing with nullable values stored in [MapMemory].
 * The delegate returns `null` if there is no corresponding value in `MapMemory`.
 */
@Deprecated(
    message = "This accessor is not needed anymore",
    replaceWith = ReplaceWith("this"),
)
public inline fun <reified T : Any> MapMemory.nullable(): MapMemoryProperty<T?> {
    return object : MapMemoryProperty<T?>() {
        override fun getValue(key: String): T? = getWithNullabilityInference(key)
        override fun setValue(key: String, value: T?) = putNotNull(key, value)
    }
}
