package org.xodium.vanillaplus.data

import net.kyori.adventure.inventory.Book
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents the data structure for a book in the game. */
internal data class BookData(
    /** The command associated with the book. */
    val cmd: String,
    /** The default permission level for this book's command. */
    val permission: PermissionDefault = PermissionDefault.TRUE,
    /** The title of the book. Defaults to the command name with the first letter capitalized and formatted. */
    private val title: String = "<fire>${cmd.replaceFirstChar { it.uppercase() }}</fire>",
    /** The author of the book. Defaults to the name of the main plugin instance class. */
    private val author: String = instance.javaClass.simpleName,
    /** The content of the book as a list of pages, where each page is a list of lines. */
    private val pages: List<List<String>>,
) {
    /**
     * Converts this [BookData] instance to a [Book] instance.
     * @return A [Book] instance with the properties of this [BookData].
     */
    fun toBook(): Book =
        Book.book(MM.deserialize(title), MM.deserialize(author), pages.map { MM.deserialize(it.joinToString("\n")) })
}
