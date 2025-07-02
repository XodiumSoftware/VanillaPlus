/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.Utils.tryCatch

/** Represents a module handling quests mechanics within the system. */
class QuestModule : ModuleInterface<QuestModule.Config> {
    override val config: Config = Config()

    private val invTitle = "Quests".fireFmt().mm()

    override fun enabled(): Boolean = config.enabled

    override fun cmds(): CommandData? {
        return CommandData(
            listOf(
                Commands.literal("quests")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx -> ctx.tryCatch { quests() } }
            ),
            "Lists all quests",
            listOf("q")
        )
    }

    override fun perms(): List<Permission> {
        return listOf(
            Permission(
                "${instance::class.simpleName}.quests.use".lowercase(),
                "Allows use of the quests command",
                PermissionDefault.TRUE
            )
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: InventoryClickEvent) {
        if (!enabled()) return
        if (event.view.title() == invTitle) event.isCancelled = true
    }

    /**
     * Creates an inventory for quests.
     * @return an [Inventory] with 5 slots, each containing a quest item.
     */
    private fun quests(): Inventory {
        return instance.server.createInventory(null, InventoryType.HOPPER, invTitle).apply {
            setItem(0, questItem(Material.WRITABLE_BOOK, "Easy".fireFmt(), listOf()))
            setItem(1, questItem(Material.WRITABLE_BOOK, "Easy".fireFmt(), listOf()))
            setItem(2, questItem(Material.WRITABLE_BOOK, "Medium".fireFmt(), listOf()))
            setItem(3, questItem(Material.WRITABLE_BOOK, "Medium".fireFmt(), listOf()))
            setItem(4, questItem(Material.WRITABLE_BOOK, "Hard".fireFmt(), listOf()))
        }
    }

    /**
     * Creates a quest item with a specific material.
     * @param material the [Material] for the quest item.
     * @param itemName the name of the quest item as a [String].
     * @param lore the lore of the quest item as a [List] of [String]s.
     * @return an [ItemStack] representing the quest item.
     */
    @Suppress("UnstableApiUsage")
    private fun questItem(material: Material, itemName: String, lore: List<String>): ItemStack {
        return ItemStack.of(material).apply {
            setData(DataComponentTypes.ITEM_NAME, itemName.mm())
            setData(DataComponentTypes.LORE, ItemLore.lore(lore.mm()))
        }
    }

    data class Config(
        override var enabled: Boolean = true
    ) : ModuleInterface.Config
}