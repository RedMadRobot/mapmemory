package com.redmadrobot.mapmemory

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.ReplaySubject
import io.reactivex.rxjava3.subjects.Subject

/**
 * Creates a delegate for dealing with [ReactiveMutableMap] stored in [MapMemory].
 * The delegate returns (and stores) `ReactiveMutableMap` with [initialMap] inside and specified
 * [strategy] if there is no corresponding value in `MapMemory`.
 */
public fun <K, V : Any> MapMemory.reactiveMutableMap(
    initialMap: Map<K, V> = emptyMap(),
    strategy: ReactiveMutableMap.ReplayStrategy = ReactiveMutableMap.ReplayStrategy.REPLAY_LAST,
): MapMemoryProperty<ReactiveMutableMap<K, V>> {
    return invoke { ReactiveMutableMap(initialMap, strategy) }
}

/**
 * Wrapper around the given map allowing to access map values in reactive style
 * using [observable], [valuesObservable] and [getValueObservable].
 *
 * Elements in ReactiveMap are stored in the order they were added.
 * @see reactiveMutableMap
 */
@Suppress("TooManyFunctions")
public class ReactiveMutableMap<K, V : Any>(
    map: Map<K, V> = emptyMap(),
    strategy: ReplayStrategy = ReplayStrategy.REPLAY_LAST,
) : MutableMap<K, V> {

    private val map = map.toMutableMap()
    private val subject: Subject<Map<K, V>> = when (strategy) {
        ReplayStrategy.NO_REPLAY -> PublishSubject.create()
        ReplayStrategy.REPLAY_LAST -> BehaviorSubject.createDefault(map.toMap())
        ReplayStrategy.REPLAY_ALL -> ReplaySubject.create<Map<K, V>>().apply { onNext(map.toMap()) }
    }

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

    /** Returns an [Observable] with the whole map. Emits new value when map content is changed. */
    public val observable: Observable<Map<K, V>> = subject

    /** Replaces all values in this map with key/value pairs from the specified map [from]. */
    public fun replaceAll(from: Map<K, V>) {
        change {
            clear()
            putAll(from)
        }
    }

    /**
     * Returns an [Observable] for the value corresponding to the given [key] or `Observable.empty()`
     * if such a key is not present in the map or is `null`.
     */
    @Deprecated(
        message = "Replaced with getValueObservable",
        replaceWith = ReplaceWith("getValueObservable(key)")
    )
    public fun getStream(key: K): Observable<V> {
        return subject.flatMap { map ->
            val entity = map[key]
            if (entity != null) Observable.just(entity) else Observable.empty()
        }
    }

    /** Returns a [List] of all values in this map. */
    @Deprecated(
        message = "Use values field",
        replaceWith = ReplaceWith("values.toList()"),
    )
    public fun getAll(): List<V> = values.toList()

    /** Returns an [Observable] for a [List] of all values in this map. */
    @Deprecated(
        message = "Use valuesObservable field",
        replaceWith = ReplaceWith("valuesObservable.map { it.toList() }"),
    )
    public fun getAllStream(): Observable<List<V>> = valuesObservable.map { it.toList() }

    /**
     * Synchronised modification of the map.
     *
     * You shouldn't do time-consuming operations in block [transform] because access to the map
     * will be blocked until this function executed.
     */
    @Synchronized
    public fun <T> change(transform: MutableMap<K, V>.() -> T): T {
        return map.transform().also {
            subject.onNext(map.toMap())
        }
    }

    /** Values reply strategy. */
    public enum class ReplayStrategy {
        /** Subscriber will not receive values emitted before subscription. */
        NO_REPLAY,

        /** Subscriber will receive last value emitted before subscription. */
        REPLAY_LAST,

        /** Subscriber will receive all values emitted before subscription. */
        REPLAY_ALL
    }
}

/**
 * Returns an [Observable] with collection of all values in this map.
 * @see ReactiveMutableMap.values
 */
public val <K, V : Any> ReactiveMutableMap<K, V>.valuesObservable: Observable<Collection<V>>
    get() = observable.map { it.values }

/**
 * Returns an [Observable] for the value corresponding to the given [key].
 * Emits new value when value corresponding to the given key is changed in the map.
 *
 * If value is not present in this map (or was removed), nothing will be emitted.
 * @see Map.getValue
 */
public fun <K, V : Any> ReactiveMutableMap<K, V>.getValueObservable(key: K): Observable<V> {
    return observable.flatMap { map ->
        val value = map[key]
        if (value != null) Observable.just(value) else Observable.empty()
    }.distinctUntilChanged()
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
    replaceWith = ReplaceWith("reactiveMutableMap<String, T>(strategy)"),
)
@Suppress("Deprecation")
public fun <T : Any> MapMemory.reactiveMap(
    strategy: ReactiveMutableMap.ReplayStrategy = ReactiveMutableMap.ReplayStrategy.REPLAY_LAST,
): MapMemoryProperty<ReactiveMap<T>> {
    return invoke { ReactiveMap(strategy = strategy) }
}
