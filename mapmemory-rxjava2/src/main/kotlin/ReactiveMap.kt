package com.redmadrobot.mapmemory

import com.redmadrobot.mapmemory.internal.getOrPutProperty
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import io.reactivex.subjects.Subject
import kotlin.properties.ReadWriteProperty

/**
 * Key/value storage of entities with type [T], which able to be accessed in reactive style.
 * If you don't want reactive access, use [map].
 *
 * For synchronized access, use [set], [get] and [getAll].
 * For reactive access use [getLive] and [getAllLive].
 *
 * Elements in ReactiveMap are stored in the order they were added.
 * @see reactiveMap
 */
@Suppress("TooManyFunctions")
public class ReactiveMap<T : Any> internal constructor(strategy: ReplayStrategy) {

    private val internalMap = mutableMapOf<String, T>()
    private val subject: Subject<Map<String, T>> = when (strategy) {
        ReplayStrategy.NO_REPLAY -> PublishSubject.create()
        ReplayStrategy.REPLAY_LAST -> BehaviorSubject.createDefault(internalMap)
        ReplayStrategy.REPLAY_ALL -> ReplaySubject.create()
    }

    /** Returns the value corresponding to the given [key] or `null` if such a key is not present in the map. */
    @Synchronized
    public operator fun get(key: String): T? = internalMap[key]

    /**
     * Returns an [Observable] for the value corresponding to the given [key] or `Observable.empty()`
     * if such a key is not present in the map or is `null`.
     */
    public fun getLive(key: String): Observable<T> {
        return subject.flatMap { map ->
            val entity = map[key]
            if (entity != null) Observable.just(entity) else Observable.empty()
        }
    }

    /** Returns a [List] of all values in this map. */
    @Synchronized
    public fun getAll(): List<T> = internalMap.values.toList()

    /** Returns an [Observable] for a [List] of all values in this map. */
    public fun getAllLive(): Observable<List<T>> = subject.map { it.values.toList() }

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
        subject.onNext(internalMap.toMap())
    }

    /** Checks if the map contains the given [key]. */
    @Synchronized
    public operator fun contains(key: String): Boolean = key in internalMap

    /** Checks if the map is empty. */
    @Synchronized
    public fun isEmpty(): Boolean = internalMap.isEmpty()

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
 * Creates a delegate for dealing with [ReactiveMap] stored in [MapMemory].
 * The delegate returns (and stores) empty `ReactiveMap` with specified [strategy]
 * if there is no corresponding value in memory.
 */
public fun <T : Any> MapMemory.reactiveMap(
    strategy: ReactiveMap.ReplayStrategy = ReactiveMap.ReplayStrategy.REPLAY_LAST
): ReadWriteProperty<Any?, ReactiveMap<T>> {
    return getOrPutProperty { ReactiveMap<T>(strategy) }
}
