package com.redmadrobot.mapmemory

import com.redmadrobot.mapmemory.internal.getOrPutProperty
import com.redmadrobot.mapmemory.internal.getWithNullabilityInference
import com.redmadrobot.mapmemory.internal.keyOf
import com.redmadrobot.mapmemory.internal.putNotNull
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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
public open class MapMemory private constructor(
    map: ConcurrentHashMap<String, Any>,
) : MutableMap<String, Any> by map {

    /** Extension point. Gives ability to create extensions on companion. */
    public companion object;

    /** Creates a new empty [MapMemory]. */
    public constructor() : this(ConcurrentHashMap())

    /** Creates a new [MapMemory] with the content from the given [map]. */
    public constructor(map: Map<String, Any>) : this(ConcurrentHashMap(map))

    public inline operator fun <reified V : Any> invoke(
        crossinline defaultValue: () -> V,
    ): MapMemoryProperty<V> = getOrPutProperty(defaultValue)

    public inline operator fun <reified V> getValue(thisRef: Any?, property: KProperty<*>): V {
        return getWithNullabilityInference(keyOf(thisRef, property))
    }

    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun <V> setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        putNotNull(keyOf(thisRef, property), value)
    }

    /**
     * Kotlin's [withDefault][kotlin.collections.withDefault] is not compatible with MapMemory
     * so it is banned from use.
     * Use operator [invoke] when you want to put default value to memory if there no value
     * corresponding to the property key.
     * @see invoke
     */
    @Deprecated(
        level = DeprecationLevel.ERROR,
        message = "Use operator 'invoke' to get memory property with default value",
        replaceWith = ReplaceWith("this(defaultValue)"),
    )
    @Suppress("UNUSED_PARAMETER")
    public fun <V> withDefault(defaultValue: (key: String) -> V): MutableMap<String, V> {
        throw UnsupportedOperationException("Should not be called")
    }
}

/** Delegate for memory properties. */
public abstract class MapMemoryProperty<V> @PublishedApi internal constructor() : ReadWriteProperty<Any?, V> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): V = getValue(keyOf(thisRef, property))
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        setValue(keyOf(thisRef, property), value)
    }

    internal abstract fun getValue(key: String): V
    internal abstract fun setValue(key: String, value: V)
}
