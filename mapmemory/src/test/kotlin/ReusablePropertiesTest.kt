package com.redmadrobot.mapmemory

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

internal class ReusablePropertiesTest {

    private val memory = MapMemory()

    @Test
    fun `when cleared memory containing mutable list - should keep the list and clear it`() {
        val list by memory.mutableList<Int>()

        // Keep the reference to the original list
        val originalList = list
        list += 0

        memory.clear()
        list += 42

        list shouldBe originalList
        list.shouldContainExactly(42)
    }

    @Test
    fun `when cleared memory containing mutable map - should keep the map and clear it`() {
        val map by memory.mutableMap<String, Int>()

        // Keep the reference to the original map
        val originalMap = map
        map["Zero"] = 0

        memory.clear()
        map["Answer"] = 42

        map shouldBe originalMap
        map shouldContainExactly mapOf("Answer" to 42)
    }
}
