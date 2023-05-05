package com.redmadrobot.mapmemory

import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ReactiveMutableMapTest {

    private val memory = MapMemory()

    @Test
    fun `when map initialized - should emit initial value to observable first`() {
        val initialMap = mapOf("initial" to 42)

        val map by memory.reactiveMutableMap { initialMap }

        map.observable.test()
            .assertValue(initialMap)
            .dispose()
    }

    @Test
    fun `when cleared memory containing reactive map - should keep the same map and set default value`() {
        val initialMap = mapOf("initial" to 42)

        val map by memory.reactiveMutableMap { initialMap }

        // Keep the original reference to the map
        val originalMap = map
        map["some_value"] = 1

        memory.clear()

        map shouldBe originalMap
        map shouldContainExactly initialMap
    }
}
