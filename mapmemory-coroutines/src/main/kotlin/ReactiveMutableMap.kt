@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.redmadrobot.mapmemory

import kotlinx.coroutines.flow.*

/**
 * Key/value storage of entities with type [V], which able to be accessed in reactive style.
 * If you don't want reactive access, use [map].
 *
 * For synchronized access, use [set], [get] and [getAll].
 * For reactive access use [getStream] and [getAllStream].
 *
 * Elements in ReactiveMap are stored in the order they were added.
 * @see reactiveMap
 */
@Suppress("TooManyFunctions")
public class ReactiveMutableMap<K, V> {

    private val internalMap = mutableMapOf<String, V>()
    private val flow = MutableStateFlow(internalMap.toMap())

    /** Returns the value corresponding to the given [key] or `null` if such a key is not present in the map. */
    @Synchronized
    public operator fun get(key: String): V? = internalMap[key]

    /** Returns the value for the given [key] or throws an exception if there no such key in the map. */
    @Synchronized
    public fun getValue(key: String): V = internalMap.getValue(key)

    /**
     * Returns a [Flow] for the value corresponding to the given [key] or empty flow if such a
     * key is not present in the map.
     */
    public fun getStream(key: String): Flow<V> {
        return flow.asStateFlow()
            .filter { key in it }
            .map { it.getValue(key) }
    }

    /** Returns a [List] of all values in this map. */
    @Synchronized
    public fun getAll(): List<V> = internalMap.values.toList()

    /** Returns a [Flow] for a [List] of all values in this map. */
    public fun getAllStream(): Flow<List<V>> = flow.map { it.values.toList() }

    /** Associates the specified [value] with the specified [key] in the map. */
    public operator fun set(key: String, value: V) {
        change { this[key] = value }
    }

    /** Replaces all values in this map with key/value pairs from the specified map [from]. */
    public fun replaceAll(from: Map<String, V>) {
        change {
            clear()
            putAll(from)
        }
    }

    /** Updates this map with key/value pairs from the specified map [from]. */
    public fun putAll(from: Map<String, V>) {
        change {
            putAll(from)
        }
    }

    /** Removes all elements from this map. */
    public fun clear() {
        change { clear() }
    }

    /** Removes the specified key and its corresponding value from this map. */
    public fun remove(key: String) {
        change { remove(key) }
    }

    /**
     * Synchronised modification of the map.
     *
     * You shouldn't do time-consuming operations in block [transform] because access to the map
     * will be blocked until this function executed.
     */
    @Synchronized
    public fun change(transform: MutableMap<String, V>.() -> Unit) {
        internalMap.transform()
        flow.value = internalMap.toMap()
    }

    /** Checks if the map contains the given [key]. */
    @Synchronized
    public operator fun contains(key: String): Boolean = key in internalMap

    /** Checks if the map is empty. */
    @Synchronized
    public fun isEmpty(): Boolean = internalMap.isEmpty()
}

/**
 * Creates a delegate for dealing with [ReactiveMutableMap] stored in [MapMemory].
 * The delegate returns (and stores) empty `ReactiveMutableMap` if there is no corresponding value in memory.
 *
 * It is implemented using [StateFlow].
 */
public fun <K, V> MapMemory.reactiveMutableMap(): MapMemoryProperty<ReactiveMutableMap<K, V>> {
    return invoke { ReactiveMutableMap() }
}

/** @see ReactiveMutableMap */
@Deprecated(
    message = "Renamed to ReactiveMutableMap",
    replaceWith = ReplaceWith("ReactiveMutableMap<String, T>"),
)
public typealias ReactiveMap<T> = ReactiveMutableMap<String, T>

/** @see reactiveMutableMap */
@Deprecated(
    message = "Replaced with reactiveMutableMap",
    replaceWith = ReplaceWith("reactiveMutableMap<String, T>()"),
)
@Suppress("Deprecation")
public fun <T> MapMemory.reactiveMap(): MapMemoryProperty<ReactiveMap<T>> {
    return invoke { ReactiveMap() }
}
