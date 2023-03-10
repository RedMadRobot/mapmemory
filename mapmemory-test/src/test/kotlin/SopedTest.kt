package com.redmadrobot.mapmemory

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ScopedTest {

    @Test
    fun `on scopedKeyOf - and passed T and property name - should return right key`() {
        // When
        val key = scopedKeyOf<Foo>("memoizedValue")

        // Then
        key shouldBe "com.redmadrobot.mapmemory.Foo#memoizedValue"
    }

    @Test
    fun `on scopedKeyOf - and passed property - should return right key`() {
        // When
        val key = scopedKeyOf(Foo::memoizedValue)

        // Then
        key shouldBe "com.redmadrobot.mapmemory.Foo#memoizedValue"
    }
}

class Foo(memory: MapMemory) {
    val memoizedValue: String by memory
}
