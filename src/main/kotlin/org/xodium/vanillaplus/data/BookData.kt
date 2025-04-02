/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

/**
 * Configuration data for books
 */
data class BookData(
    val title: String,
    val author: String,
    val pages: List<String>
)