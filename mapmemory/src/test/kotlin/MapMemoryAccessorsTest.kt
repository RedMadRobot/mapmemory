package com.redmadrobot.mapmemory

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
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
        assertThat(memory)
            .containsExactly(entry("fieldWithDefault", "Default"))
    }

    @Suppress("UNUSED_PARAMETER")
    private fun touch(value: Any?) {
        // just call field
    }
}
