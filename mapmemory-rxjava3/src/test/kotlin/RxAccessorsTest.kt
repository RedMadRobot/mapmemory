package com.redmadrobot.mapmemory

import io.kotest.matchers.shouldBe
import kotlin.test.Test

internal class RxAccessorsTest {

    private val memory = MapMemory()

    @Test
    fun `when cleared memory containing BehaviorSubject - should keep the same subject and clear it`() {
        val subject by memory.behaviorSubject<Int>()

        // Keep the original reference to the subject
        val originalSubject = subject
        originalSubject.onNext(1)

        memory.clear()

        subject shouldBe originalSubject
        originalSubject.test()
            .assertEmpty()
            .dispose()
    }

    @Test
    fun `when cleared memory containing BehaviorSubject - should keep the same subject and emit default value`() {
        val subject by memory.behaviorSubject { 0 }

        // Keep the original reference to the subject
        val originalSubject = subject
        subject.onNext(1)

        memory.clear()

        subject shouldBe originalSubject
        subject.test()
            .assertValue(0)
            .dispose()
    }

    @Test
    fun `when cleared memory containing PublishSubject - should keep the same subject`() {
        val subject by memory.publishSubject<Int>()

        // Keep the original reference to the subject
        val originalSubject = subject
        originalSubject.onNext(1)

        memory.clear()

        subject shouldBe originalSubject
        originalSubject.test()
            .assertEmpty()
            .dispose()
    }
}
