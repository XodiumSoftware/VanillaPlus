/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import net.kyori.adventure.inventory.Book
import org.xodium.vanillaplus.utils.ExtUtils.mm

/**
 * Represents the data structure for a book in the game.
 * @property title The title of the book.
 * @property author The author of the book.
 * @property pages The content of the book, represented as a list of strings.
 */
data class BookData(
    private val title: String,
    private val author: String,
    private val pages: List<String>,
) {
    /**
     * Converts this BookData instance to a Book instance.
     * @return A Book instance with the properties of this BookData.
     */
    fun toBook(): Book = Book.book(title.mm(), author.mm(), pages.map { it.mm() })
}