/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.utils.invunload

import java.util.*

//TODO: Move to a more generic location.
object EnumUtils {
    /**
     * Returns an EnumSet of the enum constants that match the provided regex list.
     * @param enumClass The class of the enum to search.
     * @param regexList A list of regex patterns to match against the enum constant names.
     * @return An EnumSet containing the matching enum constants.
     */
    fun <E : Enum<E>> getEnumsFromRegexList(enumClass: Class<E>, regexList: List<Regex>): EnumSet<E> {
        return enumClass.enumConstants
            ?.filter { constant -> regexList.any { it.matches(constant.name) } }
            ?.toCollection(EnumSet.noneOf(enumClass))
            ?: EnumSet.noneOf(enumClass)
    }
}