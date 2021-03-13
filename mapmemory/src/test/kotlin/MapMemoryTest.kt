package com.redmadrobot.mapmemory

import org.assertj.core.api.Assertions.*
import kotlin.test.Test

internal class MapMemoryTest {

    // SUT
    private val memory = MapMemory()

    @Test
    fun `when write to memory field - should add class name to record key`() {
        // Given
        val consumer = MemoryConsumer(memory)

        // When
        consumer.value = 42

        // Then
        assertThat(memory)
            .containsExactly(entry("com.redmadrobot.mapmemory.MemoryConsumer#value", 42))
    }

    @Test
    fun `when read not initialized memory field - should throw exception`() {
        // Given
        val consumer = MemoryConsumer(memory)

        // Expect
        assertThatThrownBy { consumer.value }
            .isInstanceOf(NoSuchElementException::class.java)
            .hasMessage("Key com.redmadrobot.mapmemory.MemoryConsumer#value is missing in the map.")
    }

    @Test
    fun `when read not initialized nullable memory field - should return null`() {
        // Given
        val consumer = MemoryConsumer(memory)

        // When
        val value = consumer.nullableValue

        // Then
        assertThat(value).isNull()
    }

    @Test
    fun `when write null to nullable memory field - should remove entry from memory`() {
        // Given
        val consumer = MemoryConsumer(memory)
        consumer.nullableValue = "I'm not null"
        assertThat(memory).isNotEmpty

        // When
        consumer.nullableValue = null

        // Then
        assertThat(memory).isEmpty()
    }

    @Test
    @Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    fun `when memory field declared in function - should use field name as key`() {
        // Given
        var orphan: String by memory

        // When
        orphan = "I haven't thisRef"

        // Then
        assertThat(memory)
            .containsExactly(entry("orphan", "I haven't thisRef"))
    }
}

private class MemoryConsumer(memory: MapMemory) {
    var value: Int by memory
    var nullableValue: String? by memory
}
