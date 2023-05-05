@file:Suppress("PackageDirectoryMismatch")

package io.reactivex.subjects

internal fun BehaviorSubject<*>.clear() {
    setCurrent(null)
}
