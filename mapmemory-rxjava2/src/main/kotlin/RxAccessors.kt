package com.redmadrobot.mapmemory

import io.reactivex.Maybe
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.clear

/**
 * Creates a delegate for dealing with [BehaviorSubject] stored in [MapMemory].
 * The delegate returns (and stores) new subject if there is no corresponding value in `MapMemory`.
 *
 * The property is _reusable_.
 */
public fun <T : Any> MapMemory.behaviorSubject(): MapMemoryProperty<BehaviorSubject<T>> {
    return invoke(clear = { it.clear() }) { BehaviorSubject.create() }
}

/**
 * Creates a delegate for dealing with [PublishSubject] stored in [MapMemory].
 * The delegate returns (and stores) new subject if there is no corresponding value in `MapMemory`.
 *
 * The property is _reusable_.
 */
public fun <T : Any> MapMemory.publishSubject(): MapMemoryProperty<PublishSubject<T>> {
    return invoke(clear = { /* no-op */ }) { PublishSubject.create() }
}

/**
 * Creates a delegate for dealing with [Maybe] stored in [MapMemory].
 * The delegate returns (and stores) `Maybe.empty()` if there is no corresponding value in `MapMemory`.
 */
public fun <T : Any> MapMemory.maybe(): MapMemoryProperty<Maybe<T>> {
    return invoke { Maybe.empty() }
}
