@file:Suppress("unused")

package com.redmadrobot.mapmemory

// ----------------------------------------
// Test is successful if compilation passed
// ----------------------------------------

// Condition 1. Use subclass from library class which has getValue operator
class MemorySubclass : MapMemory()

class MemoryConsumer(memory: MemorySubclass) {
    // Condition 2. Declare property using delegate
    val delegatedValue: String by memory.value()
    val delegatedNullableValue: String? by memory.value()
    // Uncomment to chek if bug in Kotlin Compiler is fixed
    // val delegatedValueWithBug: String? by memory
}

// Condition 3. Have kapt enabled and have any dependency with kapt configuration
// See build.gradle.kts
