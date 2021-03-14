package com.redmadrobot.mapmemory

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class ScopedTest {

    @Test
    fun `on scopedKeyOf - and passed T and property name - should return right key`() {
        // When
        val key = scopedKeyOf<Foo>("memoizedValue")

        // Then
        assertThat(key).isEqualTo("com.redmadrobot.mapmemory.Foo#memoizedValue")
    }

    @Test
    fun `on scopedKeyOf - and passed property - should return right key`() {
        // When
        val key = scopedKeyOf(Foo::memoizedValue)

        // Then
        assertThat(key).isEqualTo("com.redmadrobot.mapmemory.Foo#memoizedValue")
    }
}

class Foo(memory: MapMemory) {
    val memoizedValue: String by memory
}
