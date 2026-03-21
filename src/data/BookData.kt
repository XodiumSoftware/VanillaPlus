package org.xodium.vanillaplus.data

import kotlinx.serialization.Serializable
import net.kyori.adventure.inventory.Book
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.Utils.MM

/**
 * Represents the data structure for a book in the game.
 * @property title The [title] of the book.
 * @property author The [author] of the book. Defaults to the name of the main plugin instance class.
 * @property pages The content of the book, represented as a list of [pages], where each page is a list of lines.
 */
@Serializable
internal data class BookData(
    private val title: String,
    private val author: String = instance.javaClass.simpleName,
    private val pages: List<List<String>>,
) {
    /**
     * Converts this [BookData] instance to a [Book] instance.
     * @return A [Book] instance with the properties of this [BookData].
     */
    fun toBook(): Book =
        Book.book(MM.deserialize(title), MM.deserialize(author), pages.map { MM.deserialize(it.joinToString("\n")) })
}
