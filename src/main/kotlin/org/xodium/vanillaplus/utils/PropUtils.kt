package org.xodium.vanillaplus.utils

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/** Property utilities. */
internal object PropUtils {
    /**
     * Creates a delegated property that enforces values to be within a specified range.
     * @param T The type of values to validate, must implement [Comparable].
     * @param range The valid range of values (inclusive).
     * @param initial The initial value for the property. Must be within [range] or an exception will be thrown on first access.
     * @return A [ReadWriteProperty] that validates values against the specified range.
     * @throws IllegalArgumentException when attempting to set a value outside the specified range.
     */
    fun <T : Comparable<T>> inRange(
        range: ClosedRange<T>,
        initial: T,
    ) = object : ReadWriteProperty<Any?, T> {
        private var value = initial

        override fun getValue(
            thisRef: Any?,
            property: KProperty<*>,
        ) = value

        override fun setValue(
            thisRef: Any?,
            property: KProperty<*>,
            value: T,
        ) {
            require(value in range) { "${property.name} must be in $range, got $value" }
            this.value = value
        }
    }
}
