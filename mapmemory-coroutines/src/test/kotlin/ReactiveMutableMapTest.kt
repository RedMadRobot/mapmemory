@file:OptIn(ExperimentalCoroutinesApi::class)

package com.redmadrobot.mapmemory

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ReactiveMutableMapTest {

    private val memory = MapMemory()

    @Test
    fun `when map initialized - should emit initial value to flow first`() = runTest {
        val initialMap = mapOf("initial" to 42)

        val map by memory.reactiveMutableMap(initialMap)

        map.flow.test {
            awaitItem() shouldBe initialMap
        }
    }
}
