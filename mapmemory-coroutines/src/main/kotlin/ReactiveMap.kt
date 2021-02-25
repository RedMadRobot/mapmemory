@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.redmadrobot.mapmemory

import com.redmadrobot.mapmemory.internal.getOrPutProperty
import kotlinx.coroutines.flow.*
import kotlin.properties.ReadWriteProperty

/**
 * Key/value storage of entities with type [T], which able to be accessed in reactive style.
 * If you don't want reactive access, use [map].
 *
 * For synchronized access, use [set], [get] and [getAll].
 * For reactive access use [getStream] and [getAllStream].
 *
 * Elements in ReactiveMap are stored in the order they were added.
 * @see reactiveMap
 */
@Suppress("TooManyFunctions")
public class ReactiveMap<T> {

    private val internalMap = mutableMapOf<String, T>()
    private val flow = MutableStateFlow(internalMap.toMap())

    /** Returns the value corresponding to the given [key] or `null` if such a key is not present in the map. */
    @Synchronized
    public operator fun get(key: String): T? = internalMap[key]

    /** Returns the value for the given [key] or throws an exception if there no such key in the map. */
    @Synchronized
    public fun getValue(key: String): T = internalMap.getValue(key)

    /**
     * Returns a [Flow] for the value corresponding to the given [key] or empty flow if such a
     * key is not present in the map.
     */
    public fun getStream(key: String): Flow<T> {
        return flow.asStateFlow()
            .filter { key in it }
            .map { it.getValue(key) }
    }

    /** Returns a [List] of all values in this map. */
    @Synchronized
    public fun getAll(): List<T> = internalMap.values.toList()

    /** Returns a [Flow] for a [List] of all values in this map. */
    public fun getAllStream(): Flow<List<T>> = flow.map { it.values.toList() }

    /** Associates the specified [value] with the specified [key] in the map. */
    public operator fun set(key: String, value: T) {
        change { this[key] = value }
    }

    /** Replaces all values in this map with key/value pairs from the specified map [from]. */
    public fun replaceAll(from: Map<String, T>) {
        change {
            clear()
            putAll(from)
        }
    }

    /** Updates this map with key/value pairs from the specified map [from]. */
    public fun putAll(from: Map<String, T>) {
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
    public fun change(transform: MutableMap<String, T>.() -> Unit) {
        internalMap.transform()
        flow.tryEmit(internalMap.toMap())
    }

    /** Checks if the map contains the given [key]. */
    @Synchronized
    public operator fun contains(key: String): Boolean = key in internalMap

    /** Checks if the map is empty. */
    @Synchronized
    public fun isEmpty(): Boolean = internalMap.isEmpty()
}

/**
 * Creates a delegate for dealing with [ReactiveMap] stored in [MapMemory].
 * The delegate returns (and stores) empty `ReactiveMap` with specified settings
 * if there is no corresponding value in memory.
 *
 * It implemented using [StateFlow].
 */
public fun <T> MapMemory.reactiveMap(): ReadWriteProperty<Any?, ReactiveMap<T>> {
    return getOrPutProperty { ReactiveMap() }
}
