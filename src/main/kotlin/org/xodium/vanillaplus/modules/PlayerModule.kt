package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import io.papermc.paper.datacomponent.item.ResolvableProfile
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.pdcs.PlayerPDC.nickname
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt

/** Represents a module handling player mechanics within the system. */
internal class PlayerModule(
    private val tabListModule: TabListModule,
) : ModuleInterface<PlayerModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled && tabListModule.enabled()

    override fun cmds(): List<CommandData> =
        listOf(
            CommandData(
                Commands
                    .literal("nickname")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx ->
                        ctx.tryCatch {
                            if (it.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                            nickname(it.sender as Player, "")
                        }
                    }.then(
                        Commands
                            .argument("name", StringArgumentType.greedyString())
                            .executes { ctx ->
                                ctx.tryCatch {
                                    if (it.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                                    nickname(it.sender as Player, StringArgumentType.getString(ctx, "name"))
                                }
                            },
                    ),
                "Allows players to set or remove their nickname",
                listOf("nick"),
            ),
        )

    override fun perms(): List<Permission> =
        listOf(
            Permission(
                "${instance::class.simpleName}.nickname".lowercase(),
                "Allows use of the nickname command",
                PermissionDefault.TRUE,
            ),
        )

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        if (!enabled()) return

        val player = event.player

        player.displayName(player.nickname?.mm())

        if (config.i18n.playerJoinMsg.isEmpty()) return

        event.joinMessage(null)

        instance.server.onlinePlayers
            .filter { it.uniqueId != player.uniqueId }
            .forEach {
                it.sendMessage(
                    config.i18n.playerJoinMsg.mm(
                        Placeholder.component("player", player.displayName()),
                    ),
                )
            }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerQuitEvent) {
        if (!enabled() || config.i18n.playerQuitMsg.isEmpty()) return

        event.quitMessage(config.i18n.playerQuitMsg.mm(Placeholder.component("player", event.player.displayName())))
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerDeathEvent) {
        if (!enabled()) return

        val killer = event.entity.killer ?: return

        if (Math.random() < config.skullDropChance) {
            event.entity.world.dropItemNaturally(
                event.entity.location,
                playerSkull(event.entity, killer),
            )
        }
        // TODO
//        if (config.i18n.playerDeathMsg.isNotEmpty()) event.deathMessage()
//        if (config.i18n.playerDeathScreenMsg.isNotEmpty()) event.deathScreenMessageOverride()
    }

    @EventHandler
    fun on(event: PlayerAdvancementDoneEvent) {
        if (!enabled() || config.i18n.playerAdvancementDoneMsg.isEmpty()) return

        event.message(
            config.i18n.playerAdvancementDoneMsg.mm(
                Placeholder.component("player", event.player.displayName()),
                Placeholder.component("advancement", event.advancement.displayName()),
            ),
        )
    }

    @EventHandler
    fun on(event: InventoryClickEvent) {
        if (!enabled() ||
            event.click != config.enderChestClickType ||
            event.currentItem?.type != Material.ENDER_CHEST ||
            event.clickedInventory?.type != InventoryType.PLAYER
        ) {
            return
        }

        event.isCancelled = true

        instance.server.scheduler.runTask(
            instance,
            Runnable { event.whoClicked.openInventory(event.whoClicked.enderChest) },
        )
    }

    @EventHandler(ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) {
        if (!enabled()) return

        xpToBottle(event)
    }

    @EventHandler
    fun on(event: BlockBreakEvent) {
        if (!enabled()) return

        replant(event.block)
    }

    /**
     * Automatically replants a crop block after it has been fully grown and harvested.
     * @param block The block that was broken.
     */
    private fun replant(block: Block) {
        if (block.blockData !is Ageable) return

        val ageable = block.blockData as Ageable

        if (ageable.age < ageable.maximumAge) return

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                val blockType = block.type
                block.type = blockType
                ageable.age = 0
                block.blockData = ageable
            },
            2,
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
        player.displayName(player.nickname?.mm())
        tabListModule.updatePlayerDisplayName(player)
        player.sendActionBar(config.i18n.nicknameUpdated.mm(Placeholder.component("nickname", player.displayName())))
    }

    /**
     * Creates a custom player skull item when a player is killed.
     * @param entity The player whose head is being created.
     * @param killer The player who killed the entity.
     * @return An [ItemStack] representing the customized player head.
     */
    @Suppress("UnstableApiUsage")
    private fun playerSkull(
        entity: Player,
        killer: Player,
    ): ItemStack =
        ItemStack.of(Material.PLAYER_HEAD).apply {
            setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(entity.playerProfile))
            setData(
                DataComponentTypes.CUSTOM_NAME,
                config.i18n.playerHeadName.mm(Placeholder.component("player", entity.name.mm())),
            )
            setData(
                DataComponentTypes.LORE,
                ItemLore
                    .lore(
                        config.i18n.playerHeadLore
                            .mm(
                                Placeholder.component("player", entity.name.mm()),
                                Placeholder.component("killer", killer.name.mm()),
                            ),
                    ),
            )
        }

    data class Config(
        var enderChestClickType: ClickType = ClickType.SHIFT_RIGHT,
        var skullDropChance: Double = 0.1,
        var xpCostToBottle: Int = 11,
        var i18n: I18n = I18n(),
    ) : ModuleInterface.Config {
        data class I18n(
            var playerHeadName: String = "<player>’s Skull",
            var playerHeadLore: List<String> = listOf("<player> killed by <killer>"),
//            var playerDeathMsg: String = "<killer> ${"⚔".mangoFmt(true)} <player>",
            var playerJoinMsg: String = "<green>➕<reset> ${"›".mangoFmt(true)} <player>",
            var playerQuitMsg: String = "<red>➖<reset> ${"›".mangoFmt(true)} <player>",
            var playerDeathMsg: String = "☠ ${"›".mangoFmt(true)}",
            var playerDeathScreenMsg: String = "☠",
            var playerAdvancementDoneMsg: String =
                "\uD83C\uDF89 ${
                    "›".mangoFmt(
                        true,
                    )
                } <player> ${"has made the advancement:".mangoFmt()} <advancement>".mangoFmt(),
            var nicknameUpdated: String = "Nickname has been updated to: <nickname>".fireFmt(),
        )
    }
}
