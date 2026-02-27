package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.block.ChiseledBookshelf
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents a module handling bookshelf mechanics within the system. */
internal object BookshelfModule : ModuleInterface {
    @EventHandler
    fun on(event: PlayerInteractEvent) {
        val player = event.player

        if (!player.isSneaking) return

        val bookshelf = event.clickedBlock?.state as? ChiseledBookshelf ?: return

        event.isCancelled = true

        val inventory = bookshelf.snapshotInventory

        player.sendMessage(MM.deserialize("<gradient:#FFA751:#FFE259><b>Chiseled Bookshelf Content:</b></gradient>"))

        var hasItems = false

        for (i in 0 until inventory.size) {
            val item = inventory.getItem(i) ?: continue

            hasItems = true

            val slotNumber = i + 1

            when (val itemMeta = item.itemMeta) {
                is BookMeta -> {
                    val title = itemMeta.title ?: "Untitled"
                    val author = itemMeta.author

                    if (author != null) {
                        player.sendMessage(
                            MM.deserialize(
                                "<yellow>$slotNumber.</yellow> <white>$title</white><gray> by $author</gray>",
                            ),
                        )
                    } else {
                        player.sendMessage(
                            MM.deserialize("<yellow>$slotNumber.</yellow> <white>$title</white>"),
                        )
                    }
                }

                is EnchantmentStorageMeta -> {
                    val enchantments = itemMeta.storedEnchants

                    if (enchantments.isEmpty()) {
                        player.sendMessage(
                            MM.deserialize("<yellow>$slotNumber.</yellow> <white>Empty Enchanted Book</white>"),
                        )
                    } else {
                        val enchantmentList =
                            enchantments.entries.joinToString(", ") {
                                "${it.key.key.key} ${it.value}"
                            }

                        player.sendMessage(
                            MM.deserialize(
                                "<yellow>$slotNumber.</yellow> <white>Enchanted Book</white><gray> | $enchantmentList</gray>",
                            ),
                        )
                    }
                }

                else -> {
                    val itemName =
                        item.type.name
                            .lowercase()
                            .replace('_', ' ')

                    player.sendMessage(MM.deserialize("<yellow>$slotNumber.</yellow> <white>$itemName</white>"))
                }
            }
        }

        if (!hasItems) player.sendMessage(MM.deserialize("<gray>Empty</gray>"))
    }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
    )
}
