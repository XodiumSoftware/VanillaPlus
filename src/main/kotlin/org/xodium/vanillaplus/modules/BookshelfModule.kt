package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import org.bukkit.Material
import org.bukkit.block.ChiseledBookshelf
import org.bukkit.block.data.Directional
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents a module handling bookshelf mechanics within the system. */
internal object BookshelfModule : ModuleInterface {
    init {
        instance.server.scheduler.runTaskTimer(instance, Runnable { bookshelf() }, 0, 2)
    }

    /** Runs the bookshelf action bar logic for all online players. */
    private fun bookshelf() {
        val players = instance.server.onlinePlayers

        for (player: Player in players) {
            val rayTrace = player.rayTraceBlocks(6.0) ?: continue
            val block = rayTrace.hitBlock ?: continue

            if (block.type != Material.CHISELED_BOOKSHELF) continue

            val shelf = block.state as? ChiseledBookshelf ?: continue
            val hitFace = rayTrace.hitBlockFace ?: continue
            val blockData = shelf.blockData

            if (blockData !is Directional) continue
            if (blockData.facing != hitFace) continue

            val slotVector = rayTrace.hitPosition.subtract(shelf.location.toVector())
            val slot = shelf.getSlot(slotVector)
            val item = shelf.inventory.getItem(slot) ?: continue
            val meta = item.itemMeta ?: continue

            when (meta) {
                is BookMeta -> handleWrittenBook(player, meta)
                is EnchantmentStorageMeta -> handleEnchantedBook(player, meta)
                else -> player.sendActionBar(meta.displayName() ?: Component.empty())
            }
        }
    }

    /**
     * Sends action bar for a written book.
     * @param player Target player.
     * @param meta Book metadata.
     */
    private fun handleWrittenBook(
        player: Player,
        meta: BookMeta,
    ) {
        val title = meta.title ?: return
        val author = meta.author ?: return

        player.sendActionBar((meta.customName() ?: MM.deserialize(title)).append(MM.deserialize(" by $author")))
    }

    /**
     * Sends action bar for an enchanted book.
     * @param player Target player.
     * @param meta Enchantment storage metadata.
     */
    private fun handleEnchantedBook(
        player: Player,
        meta: EnchantmentStorageMeta,
    ) {
        val enchants = meta.storedEnchants

        if (enchants.isEmpty()) return

        player.sendActionBar(
            Component
                .text()
                .append(MM.deserialize("<yellow>Enchanted Book</yellow> ("))
                .append(
                    Component.join(
                        JoinConfiguration.separator(MM.deserialize(", ")),
                        enchants.map { (enchantment, level) -> enchantment.displayName(level) },
                    ),
                ).append(MM.deserialize(")"))
                .build(),
        )
    }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
    )
}
