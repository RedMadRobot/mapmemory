package com.redmadrobot.mapmemory

import com.redmadrobot.mapmemory.internal.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Memory implemented via [ConcurrentHashMap].
 * Memory responsibility is to hold data.
 * MapMemory is conception of memory built on top of [MutableMap].
 *
 * ### Access `MapMemory` values
 *
 * `MapMemory` values can be accessed via delegates:
 * ```
 * package com.example
 *
 * class TokenStorage(memory: MapMemory) {
 *     var authToken: String by memory // Use delegate for field declaration
 * }
 *
 * val memory = MapMemory()
 * val storage = TokenStorage(memory)
 * storage.authToken = "[TOKEN_HERE]"
 * println(memory) // {com.example.TokenStorage#authToken: [TOKEN_HERE]}
 * ```
 * There are number of delegates to store collections in `MapMemory`:
 * [map], [mutableMap], [list], [mutableList].
 *
 * You can specify default value using operator [invoke].
 * Default value will be used if you're trying to read property before it was written.
 * ```
 * var counter: Int by memory { 0 }
 * ```
 *
 * ### Reusable properties
 *
 * If you want to keep the same value on [MapMemory.clear] and clear the value
 * instead of removing, you can create reusable property. Such properties use
 * the given `clear` lambda to clear the current value.
 * ```
 * class Counter {
 *     fun reset() { /*...*/ }
 * }
 *
 * val counter: Counter by memory(clear = { it.reset() }) { Counter() }
 * ```
 *
 * Reusable properties are especially useful for reactive types like `Flow`
 * because you don't need to re-subscribe to flow after MapMemory was cleared.
 *
 * Many of default accessors are already return reusable properties.
 * See accessor's description to check if it returns reusable property.
 *
 * ### Scoped and shared values
 *
 * Delegate accesses `MapMemory` values by key retrieved from property name.
 * There are two types of `MapMemory` property delegates:
 * - **Scoped** to the class where the property is declared.
 *   Property key is combination of class and property name: `com.example.TokenStorage#authToken`
 * - **Shared** between all classes by the specified key.
 *   All properties are scoped by default, you can share it with the function [shared].
 */
public open class MapMemory private constructor(
    private val map: ConcurrentHashMap<String, Any>,
) : MutableMap<String, Any> by map {

    private val reusableValues: MutableMap<String, ClearableValue<out Any>> by lazy { ConcurrentHashMap() }

    /** Creates a new empty [MapMemory]. */
    public constructor() : this(ConcurrentHashMap())

    /** Creates a new [MapMemory] with the content from the given [map]. */
    public constructor(map: Map<String, Any>) : this(ConcurrentHashMap(map))

    /**
     * Returns property delegate that will initialize `MapMemory` record with the value
     * provided by [defaultValue] if it is not initialized yet.
     * ```
     * var counter: Int by memory { 0 }
     * ```
     */
    public inline operator fun <reified V : Any> invoke(
        crossinline defaultValue: () -> V,
    ): MapMemoryProperty<V> = getOrPutProperty(defaultValue)

    /**
     * Returns property delegate that will initialize `MapMemory` record with the value
     * provided by [defaultValue] if it is not initialized yet.
     * This property survives [MapMemory.clear]. The given [clear] method will be used to clear
     * the current value and reuse it instead of removing.
     * ```
     * class Counter {
     *     fun reset() { /*...*/ }
     * }
     *
     * val counter: Counter by memory(clear = { it.reset() }) { Counter() }
     * ```
     */
    public inline operator fun <reified V : Any> invoke(
        noinline clear: (V) -> Unit,
        crossinline defaultValue: () -> V,
    ): MapMemoryProperty<V> = reusableGetOrPutProperty(clear, defaultValue)

    /**
     * Returns the value of the property for the given object from this `MapMemory`.
     * If property is not found in the `MapMemory` and there no default value provided (see [invoke]):
     * - returns `null` if [V] is nullable,
     * - throws [NoSuchElementException] if [V] is non-nullable.
     *
     * @param V the value type.
     * @param thisRef the object for which the value is requested, used to get scoped property key.
     * @param property the metadata for the property, used to get the name of property and lookup
     * the value corresponding to this name in the `MapMemory`.
     * @return the property value.
     */
    public inline operator fun <reified V> getValue(thisRef: Any?, property: KProperty<*>): V {
        return getWithNullabilityInference(keyOf(thisRef, property))
    }

    /**
     * Stores the value of the property for the given object in this `MapMemory`.
     * Removes value corresponding to the key if the provided [value] is `null`.
     *
     * @param V the value type.
     * @param thisRef the object for which the value is requested, used to get scoped property key.
     * @param property the metadata for the property, used to get the name of property and lookup
     * the value corresponding to this name in the `MapMemory`.
     * @param value the value to set.
     */
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun <V> setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        putNotNull(keyOf(thisRef, property), value)
    }

    @PublishedApi
    internal fun <V : Any> putReusable(key: String, value: ClearableValue<V>) {
        reusableValues[key] = value
    }

    @PublishedApi
    internal fun <V : Any> updateReusable(key: String, value: V) {
        reusableValues.computeIfPresent(key) { _, currentValue ->
            @Suppress("UNCHECKED_CAST")
            (currentValue as ClearableValue<V>).copy(value = value)
        }
    }

    override fun clear() {
        map.clear()
        map.putAll(reusableValues.mapValues { (_, reusableValue) -> reusableValue.getCleared() })
    }

    /**
     * Kotlin's [withDefault][kotlin.collections.withDefault] is not compatible with `MapMemory`,
     * so it is banned from use.
     * Use operator [invoke] if you want to put default value to `MapMemory` if there is no value
     * corresponding to the property key.
     * @see invoke
     */
    @Deprecated(
        level = DeprecationLevel.ERROR,
        message = "Use operator 'invoke' to get MapMemory property with default value",
        replaceWith = ReplaceWith("this(defaultValue)"),
    )
    @Suppress("UNUSED_PARAMETER")
    public fun withDefault(defaultValue: (key: String) -> Any): MutableMap<String, Any> {
        throw UnsupportedOperationException("Should not be called")
    }

    /** Extension point. Gives ability to create extensions on companion. */
    public companion object
}

/** Delegate for [MapMemory] properties. */
public abstract class MapMemoryProperty<V> @PublishedApi internal constructor() : ReadWriteProperty<Any?, V> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): V = getValue(keyOf(thisRef, property))
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        setValue(keyOf(thisRef, property), value)
    }

    internal abstract fun getValue(key: String): V
    internal abstract fun setValue(key: String, value: V)
}
