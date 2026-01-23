@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ResolvableProfile
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.enchantments.*
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.PlayerMessageManager
import org.xodium.vanillaplus.pdcs.PlayerPDC.nickname
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted
import org.xodium.vanillaplus.utils.Utils.MM
import kotlin.random.Random

/** Represents a module handling player mechanics within the system. */
internal object PlayerModule : ModuleInterface {
    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("nickname")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .playerExecuted { player, _ -> nickname(player, "") }
                    .then(
                        Commands
                            .argument("name", StringArgumentType.greedyString())
                            .playerExecuted { player, ctx ->
                                nickname(player, StringArgumentType.getString(ctx, "name"))
                            },
                    ),
                "Allows players to set or remove their nickname",
                listOf("nick"),
            ),
        )

    override val perms =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.nickname".lowercase(),
                "Allows use of the nickname command",
                PermissionDefault.TRUE,
            ),
        )

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        event.player.setNickname()
        event.joinMessage(PlayerMessageManager.handleJoin(event.player) ?: return)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerQuitEvent) {
        event.quitMessage(PlayerMessageManager.handleQuit(event.player) ?: return)
    }

    @EventHandler
    fun on(event: PlayerKickEvent) {
        event.leaveMessage(PlayerMessageManager.handleKick(event.reason()) ?: return)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerDeathEvent) {
        dropPlayerHead(event.player)
        event.deathMessage(PlayerMessageManager.handleDeath(event.player, event.entity.killer) ?: return)
        event.deathScreenMessageOverride(PlayerMessageManager.handleDeathScreen())
    }

    @EventHandler
    fun on(event: PlayerAdvancementDoneEvent) {
        event.message(PlayerMessageManager.handleAdvancement(event.player, event.advancement) ?: return)
    }

    @EventHandler
    fun on(event: InventoryClickEvent) = handleEnderchest(event)

    @EventHandler(ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) {
        xpToBottle(event)
        FeatherFallingEnchantment.featherFalling(event)
    }

    @EventHandler
    fun on(event: BlockBreakEvent) {
        ReplantEnchantment.replant(event)
        SilkTouchEnchantment.silkTouch(event)
        VeinMineEnchantment.veinMine(event)
    }

    @EventHandler
    fun on(event: BlockDropItemEvent) = PickupEnchantment.pickup(event)

    @EventHandler
    fun on(event: EntityEquipmentChangedEvent) = NightVisionEnchantment.nightVision(event)

    /**
     * Handles the event when a player dies.
     * @param event The PlayerDeathEvent triggered when a player dies.
     */
    private fun dropPlayerHead(player: Player) {
        if (Random.nextDouble() > config.playerModule.skullDropChance) return

        player.world.dropItemNaturally(player.location, player.head())
    }

    /**
     * Handles the inventory click event where a player can open their ender chest by clicking on an ender chest item
     * in their inventory.
     * @param event The InventoryClickEvent triggered when a player clicks in an inventory.
     */
    private fun handleEnderchest(event: InventoryClickEvent) {
        if (event.click != config.playerModule.enderChestClickType) return
        if (event.currentItem?.type != Material.ENDER_CHEST) return
        if (event.clickedInventory?.type != InventoryType.PLAYER) return

        event.isCancelled = true

        instance.server.scheduler.runTask(
            instance,
            Runnable { event.whoClicked.openInventory(event.whoClicked.enderChest) },
        )
    }

    /**
     * Handles the interaction event where a player can convert their experience points into an experience bottle
     * if specific conditions are met.
     * @param event The PlayerInteractEvent triggered when a player interacts with the world or an object.
     */
    private fun xpToBottle(event: PlayerInteractEvent) {
        if (event.clickedBlock?.type != Material.ENCHANTING_TABLE ||
            event.item?.type != Material.GLASS_BOTTLE ||
            !event.player.isSneaking
        ) {
            return
        }

        val player = event.player

        if (player.calculateTotalExperiencePoints() < config.playerModule.xpCostToBottle) return

        player.giveExp(-config.playerModule.xpCostToBottle)
        event.item?.subtract(1)
        player.inventory
            .addItem(ItemStack.of(Material.EXPERIENCE_BOTTLE, 1))
            .values
            .forEach { player.world.dropItemNaturally(player.location, it) }
    }

    /**
     * Sets the nickname of the player to the given name.
     * @param player The player whose nickname is to be set.
     * @param name The new nickname for the player.
     */
    private fun nickname(
        player: Player,
        name: String,
    ) {
        player.nickname = name
        player.displayName(MM.deserialize(player.nickname))
        player.sendActionBar(
            MM.deserialize(
                config.playerModule.i18n.nicknameUpdated,
                Placeholder.component("nickname", player.displayName()),
            ),
        )
    }

    /** Sets the display name of the player based on their nickname. */
    private fun Player.setNickname() = displayName(MM.deserialize(nickname))

    /** Drops the player's head at their location based on the configured chance. */
    @Suppress("UnstableApiUsage")
    private fun Player.head(): ItemStack =
        ItemStack.of(Material.PLAYER_HEAD).apply {
            setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(playerProfile))
        }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
        var enderChestClickType: ClickType = ClickType.SHIFT_RIGHT,
        var skullDropChance: Double = 0.01,
        var xpCostToBottle: Int = 11,
        var silkTouch: SilkTouchEnchantment = SilkTouchEnchantment(),
        var i18n: I18n = I18n(),
    ) {
        /** Represents the settings for the Silk Touch enchantment. */
        @Serializable
        data class SilkTouchEnchantment(
            var allowSpawnerSilk: Boolean = true,
            var allowBuddingAmethystSilk: Boolean = true,
        )

        /** Represents the internationalization strings for the module. */
        @Serializable
        data class I18n(
            var playerJoinMsg: String = "<green>➕<reset> <gradient:#FFE259:#FFA751>›</gradient> <player>",
            var playerQuitMsg: String = "<red>➖<reset> <gradient:#FFE259:#FFA751>›</gradient> <player>",
            var playerDeathByPlayerMsg: String = "<killer> <gradient:#FFE259:#FFA751>⚔</gradient> <player>",
            var playerDeathScreenMsg: String = "☠",
            var playerAdvancementDoneMsg: String =
                "\uD83C\uDF89 <gradient:#FFE259:#FFA751>›</gradient> <player> " +
                    "<gradient:#FFE259:#FFA751>has made the advancement:</gradient> <advancement>",
            var playerKickMsg: String =
                "<red>❌<reset> <gradient:#FFE259:#FFA751>›</gradient> <player> " +
                    "<gradient:#FFE259:#FFA751>reason:</gradient> <reason>",
            var nicknameUpdated: String =
                "<gradient:#CB2D3E:#EF473A>Nickname has been updated to: <nickname></gradient>",
        )
    }
}
