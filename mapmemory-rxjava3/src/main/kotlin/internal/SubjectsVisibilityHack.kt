@file:Suppress("PackageDirectoryMismatch")

package io.reactivex.rxjava3.subjects

internal fun BehaviorSubject<*>.clear() {
    setCurrent(null)
}
