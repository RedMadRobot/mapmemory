package com.redmadrobot.mapmemory

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Creates a delegate for dealing with [MutableStateFlow] stored in [MapMemory].
 * The delegate returns (and stores) new StateFlow if there is no corresponding value in memory.
 */
public fun <T> MapMemory.stateFlow(initialValue: T): MapMemoryProperty<MutableStateFlow<T>> {
    return invoke { MutableStateFlow(initialValue) }
}

/**
 * Creates a delegate for dealing with [MutableSharedFlow] stored in [MapMemory].
 * The delegate returns (and stores) new SharedFlow if there is no corresponding value in memory.
 */
public fun <T> MapMemory.sharedFlow(
    replay: Int = 0,
    extraBufferCapacity: Int = 0,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
): MapMemoryProperty<MutableSharedFlow<T>> {
    return invoke { MutableSharedFlow(replay, extraBufferCapacity, onBufferOverflow) }
}
