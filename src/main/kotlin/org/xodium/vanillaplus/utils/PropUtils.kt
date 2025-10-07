package org.xodium.vanillaplus.utils

import kotlin.reflect.KProperty

/** Property utilities. */
internal object PropUtils {
    /**
     * Creates a delegated property for an Int that must stay within [range].
     * @param range The valid range of values.
     * @param initial The initial value of the property. Must be within [range].
     * @throws [IllegalArgumentException] if a value outside the range is assigned.
     */
    class IntInRangeDelegate(
        private val range: IntRange,
        initial: Int,
    ) {
        private var value = initial

        operator fun getValue(
            thisRef: Any?,
            property: KProperty<*>,
        ): Int = value

        operator fun setValue(
            thisRef: Any?,
            property: KProperty<*>,
            newValue: Int,
        ) {
            require(newValue in range) { "${property.name} must be in $range, got $newValue" }
            value = newValue
        }
    }
}
