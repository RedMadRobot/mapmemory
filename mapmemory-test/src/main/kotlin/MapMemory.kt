package com.redmadrobot.mapmemory

/**
 * Returns a new [MapMemory] with the specified content give as list of pairs.
 * You can use it in conjunction with [scopedKeyOf] to build mock `MapMemory`:
 * ```
 * class MemoryConsumer(memory: MapMemory) {
 *     var someMemoryValue: String by memory
 * }
 *
 * val mockMemory = mapMemoryOf(
 *     scopedKeyOf<MemoryConsumer>("someMemoryValue") to "Mock Value"
 * )
 * ```
 * @see mapOf
 * @see scopedKeyOf
 */
public fun mapMemoryOf(vararg pair: Pair<String, Any>): MapMemory = MapMemory(mapOf(*pair))
