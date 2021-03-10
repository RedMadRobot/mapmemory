package com.redmadrobot.mapmemory

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import kotlin.test.Test

internal class SharedMapMemoryPropertyTest {

    // SUT
    private val memory = MapMemory()

    @Test
    fun `when declared shared memory field - should save the field by passed key`() {
        // Given
        val classA = ClassA(memory)

        // When
        classA.sharedValueA = 42

        // Then
        assertThat(memory)
            .containsExactly(entry("value", 42))
    }

    @Test
    fun `when declared shared memory fields in two classes - should use the same memory property`() {
        // Given
        val classA = ClassA(memory)
        val classB = ClassB(memory)

        // When
        classA.sharedValueA = 42

        // Then
        assertThat(classB.sharedValueB).isEqualTo(42)
    }

    @Test
    fun `when declared shared memory field with accessor - should save the field by passed key`() {
        // Given
        val sharedValue: Int by memory { 0 }.shared("value")
        val classA = ClassA(memory)

        // When
        assertThat(sharedValue).isEqualTo(0)
        classA.sharedValueA += 1

        // Then
        assertThat(classA.sharedValueA).isEqualTo(1)
    }
}

private class ClassA(memory: MapMemory) {
    var sharedValueA: Int by memory.shared("value")
}

private class ClassB(memory: MapMemory) {
    var sharedValueB: Int by memory.shared("value")
}
