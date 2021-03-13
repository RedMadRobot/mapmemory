// Public API
@file:Suppress("unused")

package com.redmadrobot.mapmemory

import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject

/**
 * Creates a delegate for dealing with [BehaviorSubject] stored in [MapMemory].
 * The delegate returns (and stores) new subject if there is no corresponding value in memory.
 */
public fun <T : Any> MapMemory.behaviorSubject(): MapMemoryProperty<BehaviorSubject<T>> {
    return invoke { BehaviorSubject.create() }
}

/**
 * Creates a delegate for dealing with [PublishSubject] stored in [MapMemory].
 * The delegate returns (and stores) new subject if there is no corresponding value in memory.
 */
public fun <T : Any> MapMemory.publishSubject(): MapMemoryProperty<PublishSubject<T>> {
    return invoke { PublishSubject.create() }
}

/**
 * Creates a delegate for dealing with [Maybe] stored in [MapMemory].
 * The delegate returns (and stores) `Maybe.empty()` if there is no corresponding value in memory.
 */
public fun <T : Any> MapMemory.maybe(): MapMemoryProperty<Maybe<T>> {
    return invoke { Maybe.empty() }
}
