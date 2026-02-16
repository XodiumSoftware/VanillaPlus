package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.ChiseledBookshelf
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.material.Directional
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents a module handling bookshelf mechanics within the system. */
internal object BookshelfModule : ModuleInterface {
    private val ROMAN_MAP: List<Pair<Int, String>> =
        listOf(
            1000 to "M",
            900 to "CM",
            500 to "D",
            400 to "CD",
            100 to "C",
            90 to "XC",
            50 to "L",
            40 to "XL",
            10 to "X",
            9 to "IX",
            5 to "V",
            4 to "IV",
            1 to "I",
        )

    init {
        instance.server.scheduler.runTaskTimer(instance, Runnable { bookshelf() }, 0, 2)
    }

    private fun bookshelf() {
        for (player in instance.server.onlinePlayers) {
            val targetBlock = getTargetBlock(player)

            if (targetBlock != null && targetBlock.blockData.material == Material.CHISELED_BOOKSHELF) {
                val shelf = targetBlock.state as ChiseledBookshelf
                val lookRayTrace = player.rayTraceBlocks(6.0) ?: return
                val rayTraceBlockFace = lookRayTrace.hitBlockFace ?: return
                val blockDirectional = shelf.blockData as Directional

                if (blockDirectional.facing != rayTraceBlockFace) return

                val slotVector = lookRayTrace.hitPosition.subtract(shelf.location.toVector())
                val slot = shelf.getSlot(slotVector)
                val shelfInventory = shelf.inventory
                val bookInSlot = shelfInventory.getItem(slot) ?: return
                val bookInSlotMeta = bookInSlot.itemMeta

                if (bookInSlotMeta is BookMeta && bookInSlotMeta.hasTitle() && bookInSlotMeta.hasAuthor()) {
                    if (bookInSlotMeta.hasCustomName()) {
                        player.sendActionBar(
                            bookInSlotMeta.customName()!!.append(MM.deserialize(" by " + bookInSlotMeta.author)),
                        )
                    } else {
                        player.sendActionBar(MM.deserialize(bookInSlotMeta.title + " by " + bookInSlotMeta.author))
                    }
                } else if (bookInSlotMeta is EnchantmentStorageMeta) {
                    var bookTitle =
                        Component
                            .text()
                            .append(MM.deserialize("<yellow>Enchanted Book</yellow>"))
                            .append(MM.deserialize(" ("))
                    var i = 0

                    for ((enchantment, level) in bookInSlotMeta.storedEnchants) {
                        val namespaceString: String = enchantment.key.namespace + ":"

                        bookTitle =
                            bookTitle.append(
                                MM.deserialize(
                                    enchantment.key
                                        .toString()
                                        .replace(namespaceString, "")
                                        .replace("_", " ")
                                        .capitalizeWords(),
                                ),
                            )
                        bookTitle = bookTitle.append(MM.deserialize(" " + intToRoman(level)))

                        if (bookInSlotMeta.storedEnchants.size > 1 && i != (bookInSlotMeta.storedEnchants.size - 1)) {
                            bookTitle = bookTitle.append(MM.deserialize(", "))
                        }

                        ++i
                    }

                    bookTitle = bookTitle.append(MM.deserialize(")"))
                    player.sendActionBar(bookTitle.build())
                } else if (bookInSlotMeta != null) {
                    player.sendActionBar(bookInSlotMeta.displayName() ?: MM.deserialize(""))
                }
            }
        }
    }

    /**
     * Gets the block the player is looking at within a given range.
     * @param player The player to check.
     * @param range The maximum distance to search.
     * @return The targeted Block, or null if none is found.
     */
    private fun getTargetBlock(
        player: Player,
        range: Int = 5,
    ): Block? = player.getTargetBlockExact(range)

    /**
     * Converts an integer to its Roman numeral representation.
     * @param num The number to convert.
     * @return Roman numeral string, or an empty string if the value is out of range.
     */
    private fun intToRoman(num: Int): String {
        if (num !in 1..3999) return ""

        var n = num
        val result = StringBuilder()

        for ((value, symbol) in ROMAN_MAP) {
            while (n >= value) {
                result.append(symbol)
                n -= value
            }
        }

        return result.toString()
    }

    /**
     * Capitalizes each word in the string while keeping certain small words lowercase.
     * @param delimiter The delimiter used to split and join words.
     * @return The formatted string with capitalized words.
     */
    private fun String.capitalizeWords(delimiter: String = " "): String {
        if (isEmpty()) return this

        return split(delimiter)
            .joinToString(delimiter) { word ->
                if (word.isEmpty()) return@joinToString word

                val lower = word.lowercase()

                if (lower == "of") lower else lower.replaceFirstChar { it.titlecaseChar() }
            }
    }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
    )
}
