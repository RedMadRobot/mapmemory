package com.redmadrobot.mapmemory

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Creates a delegate for dealing with [MutableStateFlow] stored in [MapMemory].
 * The delegate returns (and stores) new `MutableStateFlow` if there is no corresponding value in `MapMemory`.
 *
 * The property is _reusable_.
 */
public fun <T> MapMemory.stateFlow(initialValue: T): MapMemoryProperty<MutableStateFlow<T>> {
    return invoke(clear = { it.value = initialValue }) { MutableStateFlow(initialValue) }
}

/**
 * Creates a delegate for dealing with [MutableSharedFlow] stored in [MapMemory].
 * The delegate returns (and stores) new `MutableSharedFlow` if there is no corresponding value in `MapMemory`.
 *
 * The property is _reusable_.
 */
public fun <T> MapMemory.sharedFlow(
    replay: Int = 0,
    extraBufferCapacity: Int = 0,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
): MapMemoryProperty<MutableSharedFlow<T>> {
    @OptIn(ExperimentalCoroutinesApi::class)
    return invoke(clear = { it.resetReplayCache() }) {
        MutableSharedFlow(replay, extraBufferCapacity, onBufferOverflow)
    }
}
