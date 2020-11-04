// Public API
@file:Suppress("unused")

package com.redmadrobot.mapmemory

import com.redmadrobot.mapmemory.internal.getOrPutProperty
import io.reactivex.Maybe
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlin.properties.ReadWriteProperty

/**
 * Creates a delegate for dealing with [BehaviorSubject] stored in [MapMemory].
 * The delegate returns (and stores) new subject if there is no corresponding value in memory.
 */
public fun <T : Any> MapMemory.behaviorSubject(): ReadWriteProperty<Any?, BehaviorSubject<T>> {
    return getOrPutProperty { BehaviorSubject.create<T>() }
}

/**
 * Creates a delegate for dealing with [PublishSubject] stored in [MapMemory].
 * The delegate returns (and stores) new subject if there is no corresponding value in memory.
 */
public fun <T : Any> MapMemory.publishSubject(): ReadWriteProperty<Any?, PublishSubject<T>> {
    return getOrPutProperty { PublishSubject.create<T>() }
}

/**
 * Creates a delegate for dealing with [Maybe] stored in [MapMemory].
 * The delegate returns (and stores) `Maybe.empty()` if there is no corresponding value in memory.
 */
public fun <T : Any> MapMemory.maybe(): ReadWriteProperty<Any?, Maybe<T>> {
    return getOrPutProperty { Maybe.empty<T>() }
}
