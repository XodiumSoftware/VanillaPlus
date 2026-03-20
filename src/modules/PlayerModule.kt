@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent
import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ResolvableProfile
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent
import io.papermc.paper.event.player.PlayerServerFullCheckEvent
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.enchantments.FeatherFallingEnchantment
import org.xodium.vanillaplus.enchantments.NightVisionEnchantment
import org.xodium.vanillaplus.enchantments.PickupEnchantment
import org.xodium.vanillaplus.enchantments.ReplantEnchantment
import org.xodium.vanillaplus.enchantments.SilkTouchEnchantment
import org.xodium.vanillaplus.enchantments.VeinMineEnchantment
import org.xodium.vanillaplus.interfaces.ModuleConfigInterface
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.PlayerMessageManager
import org.xodium.vanillaplus.pdcs.PlayerPDC.nickname
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted
import org.xodium.vanillaplus.utils.PlayerUtils.face
import org.xodium.vanillaplus.utils.Utils.MM
import org.xodium.vanillaplus.utils.Utils.configDelegate
import kotlin.random.Random

/** Represents a module handling player mechanics within the system. */
internal object PlayerModule : ModuleInterface {
    override val config by configDelegate { Config() }

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
        val player = event.player

        player.setNickname()
        joinBanner(player)
        event.joinMessage(PlayerMessageManager.handleJoin(player) ?: return)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerQuitEvent) {
        event.quitMessage(PlayerMessageManager.handleQuit(event.player) ?: return)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun on(event: PlayerServerFullCheckEvent) {
        if (event.isAllowed) return

        event.deny(PlayerMessageManager.handleServerFull() ?: return)
    }

    @Suppress("UnstableApiUsage")
    @EventHandler(priority = EventPriority.HIGH)
    fun on(event: PlayerConnectionValidateLoginEvent) {
        if (event.isAllowed) return

        event.kickMessage(PlayerMessageManager.handleLoginDenied() ?: return)
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
    fun on(event: PlayerSetSpawnEvent) {
        event.notification = PlayerMessageManager.handleSetSpawn(event.notification ?: return) ?: return
    }

    @Suppress("UnstableApiUsage")
    @EventHandler
    fun on(event: PlayerBedEnterEvent) {
        event.player.sendMessage(PlayerMessageManager.handleBedEnter(event.enterAction().problem() ?: return) ?: return)
    }

    @EventHandler
    fun on(event: PlayerInteractEvent) {
        xpToBottle(event)
        FeatherFallingEnchantment.featherFalling(event)
        handleEnderchest(event)
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
     * Sends the welcome banner to the player on join.
     * @param player The player who joined.
     */
    private fun joinBanner(player: Player) {
        var imageIndex = 0

        player.sendMessage(
            MM.deserialize(
                Regex("<image>").replace(config.welcomeText.joinToString("\n")) { "<image${++imageIndex}>" },
                Placeholder.component("player", player.displayName()),
                *player
                    .face()
                    .lines()
                    .mapIndexed { i, line -> Placeholder.component("image${i + 1}", MM.deserialize(line)) }
                    .toTypedArray(),
            ),
        )
    }

    /**
     * Attempts to drop the specified player's head at their current location.
     * @param player The player whose head may be dropped.
     */
    private fun dropPlayerHead(player: Player) {
        if (Random.nextDouble() > config.skullDropChance) return

        player.world.dropItemNaturally(player.location, player.head())
    }

    /**
     * Opens the player's ender chest when an ender chest is in the offhand and the player right-clicks in the air.
     * @param event The PlayerInteractEvent triggered by the interaction.
     */
    private fun handleEnderchest(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR) return
        if (event.item?.type != Material.ENDER_CHEST) return
        if (event.player.gameMode != GameMode.SURVIVAL) return

        event.isCancelled = true
        instance.server.scheduler.runTask(
            instance,
            Runnable { event.player.openInventory(event.player.enderChest) },
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

        if (player.calculateTotalExperiencePoints() < config.xpCostToBottle) return

        player.giveExp(-config.xpCostToBottle)
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
                config.i18n.nicknameUpdated,
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
        override var enabled: Boolean = false,
        var skullDropChance: Double = 0.01,
        var xpCostToBottle: Int = 11,
        var welcomeText: List<String> =
            listOf(
                "<gradient:#FFA751:#FFE259>]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[</gradient>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient> <gradient:#CB2D3E:#EF473A>Welcome</gradient> <player> <click:suggest_command:'/nickname '><hover:show_text:'<gradient:#FFE259:#FFA751>Set your nickname!</gradient>'><white><sprite:items:item/name_tag></white></hover></click> <click:suggest_command:'/locator '><hover:show_text:'<gradient:#FFE259:#FFA751>Change your locator color!</gradient>'><white><sprite:items:item/compass_00></white></hover></click>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient> <gradient:#CB2D3E:#EF473A>Check out</gradient><gray>:</gray>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient> <gray>✦</gray> <click:run_command:'/rules'><gradient:#13547a:#80d0c7>/rules</gradient></click:run_command>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient> <gray>✦</gray> <click:open_url:'https://illyria.fandom.com'><gradient:#13547a:#80d0c7>wiki</gradient></click:open_url>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient>",
                "<gradient:#FFA751:#FFE259>]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[</gradient>",
            ),
        var silkTouch: SilkTouchEnchantment = SilkTouchEnchantment(),
        var i18n: I18n = I18n(),
    ) : ModuleConfigInterface {
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
            var playerKickMsg: String =
                "<red>❌<reset> <gradient:#FFE259:#FFA751>›</gradient> <player> " +
                    "<gradient:#FFE259:#FFA751>reason:</gradient> <reason>",
            var playerSetSpawnMsg: String =
                "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> <notification>",
            var nicknameUpdated: String =
                "<gradient:#CB2D3E:#EF473A>Nickname has been updated to: <nickname></gradient>",
            var advancementMessages: AdvancementMessages = AdvancementMessages(),
            var bedEnterMessages: BedEnterMessages = BedEnterMessages(),
            var loginMessages: LoginMessages = LoginMessages(),
        ) {
            /** Represents the i18n messages for each advancement type. */
            @Serializable
            data class AdvancementMessages(
                var task: String =
                    "\uD83C\uDF89 <gradient:#FFE259:#FFA751>›</gradient> <player> " +
                        "<gradient:#FFE259:#FFA751>has made the advancement:</gradient> <advancement>",
                var goal: String =
                    "\uD83C\uDF89 <gradient:#FFE259:#FFA751>›</gradient> <player> " +
                        "<gradient:#FFE259:#FFA751>has reached the goal:</gradient> <advancement>",
                var challenge: String =
                    "\uD83C\uDF89 <gradient:#FFE259:#FFA751>›</gradient> <player> " +
                        "<gradient:#FFE259:#FFA751>has completed the challenge:</gradient> <advancement>",
            )

            /** Represents the i18n messages shown to players denied during login. */
            @Serializable
            data class LoginMessages(
                var full: String =
                    "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> The server is full.",
                var denied: String =
                    "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> You are not allowed to join this server.",
            )

            /** Represents the i18n messages for bed enter failure reasons. */
            @Serializable
            data class BedEnterMessages(
                var tooFarAway: String =
                    "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> You are too far away from the bed.",
                var obstructed: String =
                    "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> Your bed is obstructed.",
                var notSafe: String =
                    "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> You cannot sleep while monsters are nearby.",
                var explosion: String =
                    "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> You cannot sleep here.",
                var other: String = "",
            )
        }
    }
}
