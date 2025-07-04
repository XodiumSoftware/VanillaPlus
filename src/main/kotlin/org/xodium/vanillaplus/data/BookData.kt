/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import net.kyori.adventure.inventory.Book
import org.xodium.vanillaplus.utils.ExtUtils.mm

/**
 * Represents the data structure for a book in the game.
 * @property cmd The command associated with the book.
 * @property title The title of the book.
 * @property author The author of the book.
 * @property pages The content of the book, represented as a list of strings.
 */
data class BookData(
    val cmd: String,
    private val title: String,
    private val author: String,
    private val pages: List<String>,
) {
    /**
     * Custom deserializer for Jackson.
     * It expects each page to be a list of strings and joins them with newlines.
     * @param map A map containing the book data.
     */
    @Suppress("unused")
    @JsonCreator
    constructor(map: Map<String, Any>) : this(
        cmd = map["cmd"] as String,
        title = map["title"] as String,
        author = map["author"] as String,
        pages = (map["pages"] as List<*>).map { pageLines ->
            (pageLines as List<*>).joinToString("\n") { it.toString() }
        }
    )

    /**
     * Custom serializer for Jackson.
     * It splits each page string into a list of lines for pretty printing in JSON.
     * @return A map representation of the book data.
     */
    @Suppress("unused")
    @JsonValue
    fun toMap(): Map<String, Any> {
        return mapOf(
            "cmd" to cmd,
            "title" to title,
            "author" to author,
            "pages" to pages.map { it.split("\n") }
        )
    }

    /**
     * Converts this BookData instance to a Book instance.
     * @return A Book instance with the properties of this BookData.
     */
    fun toBook(): Book = Book.book(title.mm(), author.mm(), pages.map { it.mm() })
}