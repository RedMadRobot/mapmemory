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
 * Memory responsibility is to hold data.
 * MapMemory is conception of memory built on top of [MutableMap].
 *
 * ### Access to memory values
 *
 * Memory values can be accessed via delegates:
 * ```
 * package com.example
 *
 * class TokenStorage(memory: MapMemory) {
 *     var authToken: String by memory // Used delegate for field declaration
 * }
 *
 * val memory = MapMemory()
 * val storage = TokenStorage(memory)
 * storage.authToken = "[TOKEN_HERE]"
 * println(memory) // {com.example.TokenStorage#authToken: [TOKEN_HERE]}
 * ```
 * There are a number of delegates to store collections in memory:
 * [map], [mutableMap], [list], [mutableList].
 *
 * You can specify default value using operator [invoke].
 * Default value will used if you're trying to read property before it was written.
 * ```
 * var counter: Int by memory { 0 }
 * ```
 *
 * ### Scoped and shared values
 *
 * Delegate accesses memory values by key retrieved from property name.
 * There are two types of memory property delegates:
 * - **Scoped** to the class where the property is declared.
 *   Property key is combination of class and property name: `com.example.TokenStorage#authToken`
 * - **Shared** between all classes by the specified key.
 *   All properties are scoped by default, you can share it with the function [shared].
 */
public open class MapMemory private constructor(
    map: ConcurrentHashMap<String, Any>,
) : MutableMap<String, Any> by map {

    /** Creates a new empty [MapMemory]. */
    public constructor() : this(ConcurrentHashMap())

    /** Creates a new [MapMemory] with the content from the given [map]. */
    public constructor(map: Map<String, Any>) : this(ConcurrentHashMap(map))

    /**
     * Returns property delegate that will initialize memory record with the value
     * provided by [defaultValue] if it is not initialized yet.
     * ```
     * var counter: Int by memory { 0 }
     * ```
     */
    public inline operator fun <reified V : Any> invoke(
        crossinline defaultValue: () -> V,
    ): MapMemoryProperty<V> = getOrPutProperty(defaultValue)

    /**
     * Returns the value of the property for the given object from this memory map.
     * If property is not found in the memory and there no default value provided (see [invoke]):
     * - returns `null` if [V] is nullable,
     * - throws [NoSuchElementException] if [V] is non-nullable.
     *
     * @param thisRef the object for which the value is requested, used to get scoped property key.
     * @param property the metadata for the property, used to get the name of property and lookup
     * the value corresponding to this name in the memory.
     * @return the property value.
     */
    public inline operator fun <reified V> getValue(thisRef: Any?, property: KProperty<*>): V {
        return getWithNullabilityInference(keyOf(thisRef, property))
    }

    /**
     * Stores the value of the property for the given object in this memory map.
     * Removes value corresponding to the key if the provided [value] is `null`.
     *
     * @param thisRef the object for which the value is requested, used to get scoped property key.
     * @param property the metadata for the property, used to get the name of property and lookup
     * the value corresponding to this name in the memory.
     * @param value the value to set.
     */
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
    public fun withDefault(defaultValue: (key: String) -> Any): MutableMap<String, Any> {
        throw UnsupportedOperationException("Should not be called")
    }

    /** Extension point. Gives ability to create extensions on companion. */
    public companion object
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
