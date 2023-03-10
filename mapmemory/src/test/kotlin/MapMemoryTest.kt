package com.redmadrobot.mapmemory

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.maps.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.throwable.shouldHaveMessage
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
        memory shouldContainExactly mapOf("com.redmadrobot.mapmemory.MemoryConsumer#value" to 42)
    }

    @Test
    fun `when read not initialized memory field - should throw exception`() {
        // Given
        val consumer = MemoryConsumer(memory)

        // Expect
        shouldThrow<NoSuchElementException> { consumer.value }
            .shouldHaveMessage("Key com.redmadrobot.mapmemory.MemoryConsumer#value is missing in the map.")
    }

    @Test
    fun `when read not initialized nullable memory field - should return null`() {
        // Given
        val consumer = MemoryConsumer(memory)

        // When
        val value = consumer.nullableValue

        // Then
        value.shouldBeNull()
    }

    @Test
    fun `when write null to nullable memory field - should remove entry from memory`() {
        // Given
        val consumer = MemoryConsumer(memory)
        consumer.nullableValue = "I'm not null"
        memory.shouldNotBeEmpty()

        // When
        consumer.nullableValue = null

        // Then
        memory.shouldBeEmpty()
    }

    @Test
    fun `when memory field declared in function - should use field name as key`() {
        // Given
        var orphan: String by memory

        // When
        orphan = "I haven't thisRef"

        // Then
        memory shouldContainExactly mapOf("orphan" to "I haven't thisRef")
    }
}

private class MemoryConsumer(memory: MapMemory) {
    var value: Int by memory
    var nullableValue: String? by memory
}
