package com.redmadrobot.mapmemory

import java.util.concurrent.ConcurrentHashMap

/**
 * Memory implemented via [ConcurrentHashMap].
 *
 * Memory responsibility is to hold data.
 * MapMemory is conception of memory built on top of [MutableMap].
 *
 * Values in memory can be accessed with delegates:
 * ```
 *  class CardsInMemoryStorage(memory: MapMemory) {
 *
 *      private val cards: MutableMap<String, Card> by memory.map()
 *
 *      operator fun get(id: String): Card? = cards[id]
 *      operator fun set(id: String, card: Card) {
 *          cards[id] = card
 *      }
 *  }
 * ```
 *
 * Memory should be singleton and can be cleared when need.
 */
public open class MapMemory : MutableMap<String, Any?> by ConcurrentHashMap()
