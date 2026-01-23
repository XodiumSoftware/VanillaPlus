package org.xodium.vanillaplus.data

import kotlinx.serialization.Serializable
import net.kyori.adventure.inventory.Book
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.Utils.MM

/**
 * Represents the data structure for a book in the game.
 * @property cmd The command associated with the book.
 * @property permission The default permission level for this book's command (defaults to TRUE).
 * @property title The [title] of the book. Defaults to the command name with the first letter capitalized and formatted.
 * @property author The [author] of the book. Defaults to the name of the main plugin instance class.
 * @property pages The content of the book, represented as a list of [pages], where each page is a list of lines.
 */
@Serializable
internal data class BookData(
    val cmd: String,
    val permission: PermissionDefault = PermissionDefault.TRUE,
    private val title: String = "<fire>${cmd.replaceFirstChar { it.uppercase() }}</fire>",
    private val author: String = instance.javaClass.simpleName,
    private val pages: List<List<String>>,
) {
    /**
     * Converts this [BookData] instance to a [Book] instance.
     * @return A [Book] instance with the properties of this [BookData].
     */
    fun toBook(): Book =
        Book.book(
            MM.deserialize(title),
            MM.deserialize(author),
            pages.map { strings -> MM.deserialize(strings.joinToString("\n")) },
        )
}
