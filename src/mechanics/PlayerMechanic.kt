@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.illyriaplus.mechanics

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent
import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent
import io.papermc.paper.event.player.PlayerServerFullCheckEvent
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.weather.ThunderChangeEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.data.CommandData
import org.xodium.illyriaplus.interfaces.MechanicInterface
import org.xodium.illyriaplus.managers.PlayerMessageManager
import org.xodium.illyriaplus.managers.SpellManager
import org.xodium.illyriaplus.pdcs.PlayerPDC.nickname
import org.xodium.illyriaplus.utils.CommandUtils.playerExecuted
import org.xodium.illyriaplus.utils.PlayerUtils.face
import org.xodium.illyriaplus.utils.PlayerUtils.head
import org.xodium.illyriaplus.utils.PlayerUtils.setNickname
import org.xodium.illyriaplus.utils.Utils.MM
import org.xodium.illyriaplus.utils.Utils.weather
import kotlin.random.Random

/** Represents a module handling player mechanics within the system. */
internal object PlayerMechanic : MechanicInterface {
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
        tablist(player)
        player.playerListName(player.displayName())
        event.joinMessage(PlayerMessageManager.handleJoin(player) ?: return)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: WeatherChangeEvent) = event.world.players.forEach { tablist(it) }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: ThunderChangeEvent) = event.world.players.forEach { tablist(it) }

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

        val killer = event.entity.killer
        val deathMessage =
            if (killer != null) {
                PlayerMessageManager.handleDeath(event.player, killer)
            } else {
                PlayerMessageManager.handleDeathNoPvp(event.player, event.deathMessage())
            }

        if (deathMessage != null) event.deathMessage(deathMessage)

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
        handleEnderchest(event)
        SpellManager.handleWandInteraction(event)
    }

    @EventHandler
    fun on(event: PlayerItemHeldEvent) = SpellManager.handleWandSelection(event)

    /**
     * Sends the welcome banner to the player on join.
     * @param player The player who joined.
     */
    private fun joinBanner(player: Player) {
        var imageIndex = 0

        player.sendMessage(
            MM.deserialize(
                Regex("<image>").replace(Config.WELCOME_TEXT.joinToString("\n")) { "<image${++imageIndex}>" },
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
        if (Random.nextDouble() > Config.SKULL_DROP_CHANCE) return

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

        if (player.calculateTotalExperiencePoints() < Config.XP_COST_TO_BOTTLE) return

        player.giveExp(-Config.XP_COST_TO_BOTTLE)
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
                Config.PlayerMessages.UPDATE_NICKNAME,
                Placeholder.component("nickname", player.displayName()),
            ),
        )
    }

    /**
     * Updates the tab list header and footer for the given audience.
     * @param audience The audience to update the tab list for.
     */
    private fun tablist(audience: Audience) {
        audience.sendPlayerListHeaderAndFooter(
            MM.deserialize(Config.TabList.HEADER.joinToString("\n")),
            MM.deserialize(
                Config.TabList.FOOTER.joinToString("\n"),
                Placeholder.component(
                    "weather",
                    MM.deserialize(
                        instance.server.worlds[0].weather(
                            Config.TabList.WEATHER_THUNDERING,
                            Config.TabList.WEATHER_STORM,
                            Config.TabList.WEATHER_CLEAR,
                        ),
                    ),
                ),
            ),
        )
    }

    /** Configuration for the PlayerModule. */
    object Config {
        const val SKULL_DROP_CHANCE: Double = 0.01
        const val XP_COST_TO_BOTTLE: Int = 11
        val WELCOME_TEXT: List<String> =
            listOf(
                "<gradient:#FFA751:#FFE259>]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|" +
                    "[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[</gradient>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient> " +
                    "<gradient:#CB2D3E:#EF473A>Welcome</gradient> <player> " +
                    "<click:suggest_command:'/nickname '>" +
                    "<hover:show_text:'<gradient:#FFE259:#FFA751>Set your nickname!</gradient>'>" +
                    "<white><sprite:items:item/name_tag></white></hover></click> " +
                    "<click:suggest_command:'/locator '>" +
                    "<hover:show_text:'<gradient:#FFE259:#FFA751>Change your locator color!</gradient>'>" +
                    "<white><sprite:items:item/compass_00></white></hover></click>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient> " +
                    "<gradient:#CB2D3E:#EF473A>Check out</gradient><gray>:</gray>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient> <gray>✦</gray> " +
                    "<click:run_command:'/rules'><gradient:#13547a:#80d0c7>/rules</gradient></click:run_command>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient> <gray>✦</gray> " +
                    "<click:open_url:'https://vanillaplus.xodium.org'>" +
                    "<gradient:#13547a:#80d0c7>wiki</gradient></click:open_url>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient>",
                "<gradient:#FFA751:#FFE259>]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|" +
                    "[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[</gradient>",
            )

        /** Tab list header, footer, and weather icon configuration. */
        object TabList {
            val HEADER: List<String> =
                listOf(
                    "<gradient:#FFA751:#FFE259><st>───────────────</st></gradient> " +
                        "<gradient:#CB2D3E:#EF473A>" +
                        "\uD835\uDD74\uD835\uDD91\uD835\uDD91\uD835\uDD9E\uD835\uDD97\uD835\uDD8E\uD835\uDD86" +
                        "</gradient> " +
                        "<gradient:#FFE259:#FFA751><st>───────────────</st></gradient>",
                    "",
                )
            val FOOTER: List<String> =
                listOf(
                    "",
                    "<gradient:#FFA751:#FFE259><st>─────────────</st></gradient>  " +
                        "<gradient:#CB2D3E:#EF473A>Weather:</gradient> <weather> " +
                        " <gradient:#FFE259:#FFA751><st>─────────────</st></gradient>",
                )
            const val WEATHER_THUNDERING: String = "<red>\uD83C\uDF29<reset>"
            const val WEATHER_STORM: String = "<yellow>\uD83C\uDF26<reset>"
            const val WEATHER_CLEAR: String = "<green>\uD83C\uDF24<reset>"
        }

        /** Player join, quit, death, and kick message strings. */
        object PlayerMessages {
            const val JOIN: String = "<green>➕<reset> <gradient:#FFE259:#FFA751>›</gradient> <player>"
            const val QUIT: String = "<red>➖<reset> <gradient:#FFE259:#FFA751>›</gradient> <player>"
            const val DEATH_BY_PLAYER: String = "<killer> <gradient:#FFE259:#FFA751>⚔</gradient> <player>"
            const val DEATH: String =
                "<gradient:#FFE259:#FFA751>💀</gradient> <gradient:#FFE259:#FFA751>›</gradient> <cause>"
            const val DEATH_SCREEN: String = "☠"
            const val KICK: String =
                "<red>❌<reset> <gradient:#FFE259:#FFA751>›</gradient> <player> " +
                    "<gradient:#FFE259:#FFA751>reason:</gradient> <reason>"
            const val SET_SPAWN: String =
                "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> <notification>"
            const val UPDATE_NICKNAME: String =
                "<gradient:#CB2D3E:#EF473A>Nickname has been updated to: <nickname></gradient>"
        }

        /** Advancement completion message strings by type (task, goal, challenge). */
        object AdvancementMessages {
            const val TASK: String =
                "\uD83C\uDF89 <gradient:#FFE259:#FFA751>›</gradient> <player> " +
                    "<gradient:#FFE259:#FFA751>has made the advancement:</gradient> <advancement>"
            const val GOAL: String =
                "\uD83C\uDF89 <gradient:#FFE259:#FFA751>›</gradient> <player> " +
                    "<gradient:#FFE259:#FFA751>has reached the goal:</gradient> <advancement>"
            const val CHALLENGE: String =
                "\uD83C\uDF89 <gradient:#FFE259:#FFA751>›</gradient> <player> " +
                    "<gradient:#FFE259:#FFA751>has completed the challenge:</gradient> <advancement>"
        }

        /** Login denial messages (server full, access denied). */
        object LoginMessages {
            const val FULL: String =
                "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> " +
                    "The server is full."
            const val DENIED: String =
                "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> " +
                    "You are not allowed to join this server."
        }

        /** Bed enter failure messages by reason. */
        object BedEnterMessages {
            const val TOO_FAR_AWAY: String =
                "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> " +
                    "You are too far away from the bed."
            const val OBSTRUCTED: String =
                "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> Your bed is obstructed."
            const val NOT_SAFE: String =
                "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> " +
                    "You cannot sleep while monsters are nearby."
            const val EXPLOSION: String =
                "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> You cannot sleep here."
            const val OTHER: String = ""
        }
    }
}
