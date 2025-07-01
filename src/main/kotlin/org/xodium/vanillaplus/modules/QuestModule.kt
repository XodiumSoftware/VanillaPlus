/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.Material
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

class QuestModule : ModuleInterface<QuestModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    override fun cmds(): CommandData? {
        return CommandData(
            listOf(
                Commands.literal("quest")
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
                "${instance::class.simpleName}.quest.use".lowercase(),
                "Allows use of the quest command",
                PermissionDefault.TRUE
            )
        )
    }

    private fun quests(): Inventory {
        return instance.server.createInventory(null, InventoryType.DROPPER, "Quests".fireFmt().mm()).apply {
            setItem(0, easyQuestItem(TODO()))
            setItem(1, easyQuestItem(TODO()))
            setItem(2, mediumQuestItem(TODO()))
            setItem(3, mediumQuestItem(TODO()))
            setItem(4, hardQuestItem(TODO()))
        }
    }

    private fun easyQuestItem(lore: List<String>): ItemStack {
        @Suppress("UnstableApiUsage")
        return ItemStack.of(Material.ENCHANTED_BOOK).apply {
            setData(DataComponentTypes.ITEM_NAME, "Easy Quest".fireFmt().mm())
            setData(DataComponentTypes.LORE, ItemLore.lore(lore.mm()))
        }
    }

    private fun mediumQuestItem(lore: List<String>): ItemStack {
        @Suppress("UnstableApiUsage")
        return ItemStack.of(Material.ENCHANTED_BOOK).apply {
            setData(DataComponentTypes.ITEM_NAME, "Medium Quest".fireFmt().mm())
            setData(DataComponentTypes.LORE, ItemLore.lore(lore.mm()))
        }
    }

    private fun hardQuestItem(lore: List<String>): ItemStack {
        @Suppress("UnstableApiUsage")
        return ItemStack.of(Material.ENCHANTED_BOOK).apply {
            setData(DataComponentTypes.ITEM_NAME, "Hard Quest".fireFmt().mm())
            setData(DataComponentTypes.LORE, ItemLore.lore(lore.mm()))
        }
    }

    data class Config(
        override var enabled: Boolean = true
    ) : ModuleInterface.Config
}