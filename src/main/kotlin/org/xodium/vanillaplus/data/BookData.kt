/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

/**
 * Configuration data for books.
 * @property title The title of the book.
 * @property author The author of the book.
 * @property pages A list of pages in the book, where each page is represented as a string.
 */
data class BookData(
    val title: String,
    val author: String,
    val pages: List<String>
)