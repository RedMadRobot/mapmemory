package com.redmadrobot.mapmemory

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

internal class CoroutinesAccessorsTest {

    private val memory = MapMemory()

    @Test
    fun `when cleared memory containing StateFlow - should keep the same flow and emit default value`() {
        val flow by memory.stateFlow(0)

        // Keep the original reference to the StateFlow
        val originalFlow = flow
        originalFlow.value = 1

        memory.clear()

        flow shouldBe originalFlow
        originalFlow.value shouldBe 0
    }

    @Test
    fun `when cleared memory containing SharedFlow - should keep the same flow and clear replay cache`() {
        val flow by memory.sharedFlow<Int>(replay = 1)

        // Keep the original reference to the StateFlow
        val originalFlow = flow

        // Fill replay cache
        flow.tryEmit(1)
        flow.replayCache shouldContainExactly listOf(1)

        memory.clear()

        flow shouldBe originalFlow
        flow.replayCache.shouldBeEmpty()
    }
}
