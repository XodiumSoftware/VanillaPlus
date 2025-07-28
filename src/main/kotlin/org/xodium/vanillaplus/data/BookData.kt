package org.xodium.vanillaplus.data

import net.kyori.adventure.inventory.Book
import org.xodium.vanillaplus.utils.ExtUtils.mm

/**
 * Represents the data structure for a book in the game.
 * @property cmd The command associated with the book.
 * @property title The [title] of the book.
 * @property author The [author] of the book.
 * @property pages The content of the book, represented as a list of [pages], where each page is a list of lines.
 */
internal data class BookData(
    val cmd: String,
    private val title: String,
    private val author: String,
    private val pages: List<List<String>>,
) {
    /**
     * Converts this [BookData] instance to a [Book] instance.
     * @return A [Book] instance with the properties of this [BookData].
     */
    fun toBook(): Book = Book.book(title.mm(), author.mm(), pages.map { it.joinToString("\n").mm() })
}
