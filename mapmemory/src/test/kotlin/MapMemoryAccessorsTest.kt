package com.redmadrobot.mapmemory

import io.kotest.matchers.maps.shouldContainExactly
import kotlin.test.Test

internal class MapMemoryAccessorsTest {

    // SUT
    private val memory = MapMemory()

    @Test
    fun `when declared memory field with default value - should write default value on first read`() {
        // Given
        val fieldWithDefault: String by memory { "Default" }

        // When
        touch(fieldWithDefault)

        // Then
        memory shouldContainExactly mapOf("fieldWithDefault" to "Default")
    }

    @Suppress("UNUSED_PARAMETER")
    private fun touch(value: Any?) {
        // just call field
    }
}
