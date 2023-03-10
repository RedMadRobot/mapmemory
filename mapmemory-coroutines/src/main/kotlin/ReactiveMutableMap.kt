package com.redmadrobot.mapmemory

import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.*

/**
 * Creates a delegate for dealing with [ReactiveMutableMap] stored in [MapMemory].
 * The delegate returns (and stores) empty `ReactiveMutableMap` if there is no corresponding value in memory.
 */
public fun <K, V> MapMemory.reactiveMutableMap(): MapMemoryProperty<ReactiveMutableMap<K, V>> {
    return invoke { ReactiveMutableMap() }
}

/**
 * Wrapper around the given map allowing to access map values in reactive style
 * using [flow], [valuesFlow], [getFlow] and [getValueFlow].
 *
 * Elements in ReactiveMap are stored in the order they were added.
 * @see reactiveMutableMap
 */
@Suppress("TooManyFunctions")
public class ReactiveMutableMap<K, V>(
    map: Map<K, V> = emptyMap(),
) : MutableMap<K, V> {

    private val map = map.toMutableMap()
    private val _flow = MutableSharedFlow<Map<K, V>>(replay = 1, onBufferOverflow = DROP_OLDEST)

    // @formatter:off
    @Synchronized override fun equals(other: Any?): Boolean = map == other
    @Synchronized override fun hashCode(): Int = map.hashCode()
    @Synchronized override fun toString(): String = map.toString()
    @get:Synchronized override val size: Int get() = map.size
    @Synchronized override fun isEmpty(): Boolean = map.isEmpty()
    @Synchronized override fun containsKey(key: K): Boolean = map.containsKey(key)
    @Synchronized override fun containsValue(value: @UnsafeVariance V): Boolean = map.containsValue(value)
    @Synchronized override fun get(key: K): V? = map[key]
    @get:Synchronized override val keys: MutableSet<K> get() = map.keys
    @get:Synchronized override val values: MutableCollection<V> get() = map.values
    @get:Synchronized override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = map.entries

    override fun put(key: K, value: V): V? = change { put(key, value) }
    override fun remove(key: K): V? = change { remove(key) }
    override fun putAll(from: Map<out K, V>) { change { putAll(from) } }
    override fun clear() { change { clear() } }
    // @formatter:on

    /** Returns a [Flow] with the whole map. Emits new value when map content is changed. */
    public val flow: Flow<Map<K, V>> = _flow.asSharedFlow()

    /** Replaces all values in this map with key/value pairs from the specified map [from]. */
    public fun replaceAll(from: Map<K, V>) {
        change {
            clear()
            putAll(from)
        }
    }

    /** Returns a [Flow] for the value corresponding to the given [key]. */
    @Deprecated(
        message = "Replaced with getFlow",
        replaceWith = ReplaceWith("getValueFlow(key)"),
    )
    public fun getStream(key: K): Flow<V> = getValueFlow(key)

    /** Returns a [List] of all values in this map. */
    @Deprecated(
        message = "Use values field",
        replaceWith = ReplaceWith("values.toList()"),
    )
    public fun getAll(): List<V> = values.toList()

    /** Returns a [Flow] for a [List] of all values in this map. */
    @Deprecated(
        message = "Use valuesFlow field",
        replaceWith = ReplaceWith("valuesFlow.map { it.toList() }"),
    )
    public fun getAllStream(): Flow<List<V>> = valuesFlow.map { it.toList() }

    /**
     * Synchronised modification of the map.
     *
     * You shouldn't do time-consuming operations in block [transform] because access to the map
     * will be blocked until this function executed.
     */
    @Synchronized
    public fun <T> change(transform: MutableMap<K, V>.() -> T): T {
        return map.transform().also {
            _flow.tryEmit(map.toMap())
        }
    }
}

/**
 * Returns a [Flow] with collection of all values in this map.
 * @see ReactiveMutableMap.values
 */
public val <K, V> ReactiveMutableMap<K, V>.valuesFlow: Flow<Collection<V>>
    get() = flow.map { it.values }

/**
 * Returns a [Flow] for the value corresponding to the given [key].
 * Flow emits new value when value corresponding to the given key is changed in the map.
 *
 * If value is not present in this map (or was removed), `null` will be emitted.
 * You can use [getValueFlow] if you don't want `null` to be emitted when value not found in the map.
 *
 * @see Map.get
 * @see getValueFlow
 */
public fun <K, V> ReactiveMutableMap<K, V>.getFlow(key: K): Flow<V?> {
    return flow
        .map { it[key] }
        .distinctUntilChanged()
}

/**
 * Returns a [Flow] for the value corresponding to the given [key].
 * Flow emits new value when value corresponding to the given key is changed in the map.
 *
 * If value is not present in this map (or was removed), nothing will be emitted.
 * You can use [getFlow] if you want `null` to be emitted in this case.
 *
 * @see Map.getValue
 * @see getFlow
 */
public fun <K, V> ReactiveMutableMap<K, V>.getValueFlow(key: K): Flow<V> {
    return flow
        .filter { key in it }
        .map { it.getValue(key) }
        .distinctUntilChanged()
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
