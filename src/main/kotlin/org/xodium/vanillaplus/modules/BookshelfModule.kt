package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
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

        val inventory = bookshelf.inventory

        player.sendMessage(MM.deserialize(config.bookshelfModule.header))

        val slots = 0 until inventory.size

        if (!(slots).any { inventory.getItem(it) != null }) return

        for (i in slots) {
            val item = inventory.getItem(i) ?: continue
            val slotNumber = i + 1
            val slotPrefix =
                MM.deserialize(
                    config.bookshelfModule.slotPrefix,
                    Placeholder.component("slot", MM.deserialize(slotNumber.toString())),
                )

            when (val itemMeta = item.itemMeta) {
                is BookMeta -> {
                    val title = itemMeta.title
                    val author = itemMeta.author

                    var message =
                        slotPrefix.append(
                            MM.deserialize(
                                " <white><sprite:items:item/${if (title != null) "written_book" else "book"}></white>",
                            ),
                        )

                    if (title != null) message = message.append(MM.deserialize(" <white>$title</white>"))

                    if (author != null) message = message.append(MM.deserialize("<gray> by $author</gray>"))

                    player.sendMessage(message)
                }

                is EnchantmentStorageMeta -> {
                    val enchantments = itemMeta.storedEnchants
                    val enchantmentList = enchantments.entries.joinToString(", ") { "${it.key.key.key} ${it.value}" }

                    player.sendMessage(
                        slotPrefix.append(
                            MM.deserialize(
                                " <white><sprite:items:item/enchanted_book></white><gray> > $enchantmentList</gray>",
                            ),
                        ),
                    )
                }

                else -> {
                    val itemName = item.type.key.key

                    player.sendMessage(
                        slotPrefix.append(MM.deserialize(" <white><sprite:items:item/$itemName></white>")),
                    )
                }
            }
        }
    }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
        var header: String = "<gradient:#FFA751:#FFE259><b>Chiseled Bookshelf Content:</b></gradient>",
        var slotPrefix: String = "<gradient:#CB2D3E:#EF473A><slot>.</gradient>",
    )
}
